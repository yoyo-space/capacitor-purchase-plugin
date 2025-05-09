import Foundation
import Capacitor

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(InAppPurchasePlugin)
@available(iOS 15.0.0, *)
public class InAppPurchasePlugin: CAPPlugin, CAPBridgedPlugin {
    public let identifier = "InAppPurchasePlugin"
    public let jsName = "InAppPurchase"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "initialize", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "registerProducts", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getProduct", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "canSubscribe", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "purchase", returnType: CAPPluginReturnPromise)
    ]

    private let implementation = InAppPurchase()
    
    @available(iOS 15.0.0, *)
    @objc func initialize(_ call: CAPPluginCall) {
        implementation.initialize()
        call.resolve([:])
    }

    @available(iOS 15.0.0, *)
    @objc func registerProducts(_ call: CAPPluginCall) {
        let productIdentifiers = call.getArray("products") as! [String]
        
        Task {
            await implementation.registerProducts(productIdentifiers)
            call.resolve([:])
        }
    }
    
    @available(iOS 15.0.0, *)
    @objc func getProduct(_ call: CAPPluginCall) {
        let productIdentifier = call.getString("productId")!
        
        Task {
            if let product = await implementation.getProduct(productIdentifier) {
                call.resolve(["product": product])
            } else {
                call.reject("Product not found")
            }
        }
    }
    
    @available(iOS 15.0.0, *)
    @objc func canSubscribe(_ call: CAPPluginCall ){
        let productIdentifier = call.getString("productId")!
        let canSubscribe = implementation.canSubscribe(productIdentifier)
        call.resolve(["canSubscribe": canSubscribe])
    }
    
    @available(iOS 15.0.0, *)
    @objc func purchase(_ call: CAPPluginCall ){
        print(call)
        let productIdentifier = call.getString("productId")!
        Task {
            do {
                if let transaction = try await implementation.purchase(productIdentifier) {
                    call.resolve(transaction)
                } else {
                    call.reject("Purchase failed or was cancelled")
                }
            } catch {
                call.reject("Purchase failed: \(error.localizedDescription)")
            }
        }
    }
    
    
}
