import Foundation
import StoreKit
import Capacitor

@available(iOS 15.0.0, *)
@objc public class InAppPurchase: NSObject {
    public weak var plugin: CAPPlugin?
    var products: [Product] = []
    var subscriptions: [Product] = []
    var purchasedSubscriptions: [Product] = []
    
    var pendingTransactionListenerTask: Task<Void, Error>? = nil
    var finishTransactionListenerTask: Task<Void, Error>? = nil
    
    override init(){
        super.init()
        pendingTransactionListenerTask = pendingTransactionListener();
        finishTransactionListenerTask = finishTransactionListener();
    }
    
    deinit {
        pendingTransactionListenerTask?.cancel()
        finishTransactionListenerTask?.cancel()
    }
    
    @available(iOS 15.0.0, *)
    public func initialize() {
        pendingTransactionListenerTask = pendingTransactionListener();
        finishTransactionListenerTask = finishTransactionListener();
    }
 
    @available(iOS 15.0.0, *)
    func pendingTransactionListener() -> Task<Void, Error> {
        return Task.detached {
            for await result in Transaction.updates {
                do {
                    let transaction = try self.checkVerified(result)
                    await self.updateCustomerProductStatus()
                    
                    self.plugin?.notifyListeners("purchaseCompleted", data: ["transaction": transaction])
                    await transaction.finish()
                } catch {
                    print("Failed to verify transaction")
                }
            }
        }
    }
    
    @available(iOS 15.0.0, *)
    func finishTransactionListener() -> Task<Void, Error> {
        return Task.detached {
            for await result in Transaction.unfinished {
                do {
                    let transaction = try self.checkVerified(result)
                    await self.updateCustomerProductStatus()
                    
                    self.plugin?.notifyListeners("purchaseCompleted", data: ["transaction": transaction])
                    await transaction.finish()
                } catch {
                    print("Failed to finish transaction")
                }
            }
        }
    }
    
    
    @available(iOS 15.0.0, *)
    public func canSubscribe(_ productIdentifier: String) -> Bool {
        return !purchasedSubscriptions.contains(where: { $0.id == productIdentifier })
    }
    
    @available(iOS 15.0.0, *)
    public func registerProducts(_ productIdentifiers: [String]) async {
        do {
            let storeProducts = try await Product.products(for: productIdentifiers)
            for product in storeProducts {
                switch product.type {
                case .autoRenewable:
                    subscriptions.append(product)
                default:
                    products.append(product)
                }
            }
        } catch {
            print("Failed to load products \(error.localizedDescription)")
        }
    }
    
    @available(iOS 15.0.0, *)
    public func getProduct(_ productIdentifier: String) async -> Product? {
        return products.first(where: { $0.id == productIdentifier })
    }
    
    @available(iOS 15.0.0, *)
    public func purchase(_ productIdentifier: String) async throws -> [String: Any]? {
        guard let product = try await Product.products(for: [productIdentifier]).first else {
            print("Product not found")
            return nil
        }
        
        do {
            let result = try await product.purchase()
            
            switch result {
            case .success(let verification):
                let transaction = try checkVerified(verification)
                
                await updateCustomerProductStatus()
                
                await transaction.finish()
                
                // Convert transaction to dictionary for Capacitor
                let transactionData: [String: Any] = [
                    "id": transaction.id,
                    "originalID": transaction.originalID,
                    "productID": transaction.productID,
                    "price": transaction.price as Any,
                    "purchaseDate": transaction.purchaseDate.timeIntervalSince1970,
                    "originalPurchaseDate": transaction.originalPurchaseDate.timeIntervalSince1970,
                    "expirationDate": transaction.expirationDate?.timeIntervalSince1970 as Any,
                    "isUpgraded": transaction.isUpgraded,
                    "offerID": transaction.offerID as Any,
                    "revocationDate": transaction.revocationDate?.timeIntervalSince1970 as Any,
                    "revocationReason": transaction.revocationReason?.rawValue as Any,
                    "currency": transaction.currencyCode ?? "USD"
                ]
                
                plugin?.notifyListeners("purchaseCompleted", data: transactionData)
                return transactionData
            case .userCancelled:
                print("user cancelled transaction")
                plugin?.notifyListeners("purchaseCancelled", data: nil)
                return nil
            case .pending:
                print("pending verification")
                return nil
            default:
                print("Unknown purchase result")
                return nil
            }
        } catch {
            print("Purchase failed: \(error)")
            throw error
        }
    }
    
    @available(iOS 15.0.0, *)
    func checkVerified<T>(_ result: VerificationResult<T>) throws -> T {
        switch result {
        case .verified(let safe):
            return safe
        case .unverified:
            throw InAppPurchaseError.failedVerification
        }
    }
    
    @available(iOS 15.0.0, *)
    public func updateCustomerProductStatus() async {
        for await result in Transaction.currentEntitlements {
            do {
                let transaction = try checkVerified(result)
                switch transaction.productType {
                case .autoRenewable:
                    if let subscription = subscriptions.first(where: {$0.id == transaction.productID}) {
                        purchasedSubscriptions.append(subscription)
                    }
                case .nonRenewable, .nonConsumable, .consumable:
                    break
                default:
                    break
                }
                await transaction.finish()
            } catch {
                print("Failed updating product status: \(error)")
            }
        }
    }
}

public enum InAppPurchaseError: Error {
    case failedVerification
}
