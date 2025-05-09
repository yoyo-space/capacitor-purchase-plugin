package space.yoyo.capacitor.purchase;

import android.util.Log;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.PendingPurchasesParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.Objects;

@CapacitorPlugin(name = "InAppPurchase")
public class InAppPurchasePlugin extends Plugin {

    private InAppPurchase implementation;

    private BillingClient billingClient;

    public InAppPurchasePlugin(){}

    public final PurchasesUpdatedListener purchasesUpdatedListener = (billingResult, purchases) -> {
        JSObject response = new JSObject();
        if(purchases != null) {
            for (int i = 0; i < purchases.size(); i++) {

                Purchase currentPurchase = purchases.get(i);
                if (!currentPurchase.isAcknowledged() && billingResult.getResponseCode() == 0 && currentPurchase.getPurchaseState() != 2) {

                    AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(currentPurchase.getPurchaseToken())
                            .build();

                    billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult1 -> {
                        Log.i("Purchase ack", currentPurchase.getOriginalJson());
                        billingResult1.getResponseCode();

                        response.put("successful", billingResult1.getResponseCode());
                        response.put("id", currentPurchase.getOrderId());
                        response.put("productId", currentPurchase.getProducts().get(0));
                        response.put("token", currentPurchase.getPurchaseToken());
                        response.put("payload", currentPurchase.getOriginalJson());

                        response.put("serviceType", Objects.requireNonNull(currentPurchase.getAccountIdentifiers()).getObfuscatedAccountId());
                        response.put("serviceId", Objects.requireNonNull(currentPurchase.getAccountIdentifiers()).getObfuscatedProfileId());

                        // WARNING: Changed the notifyListeners method from protected to public in order to get the method call to work
                        // This may be a security issue in the future - in order to fix it, it may be best to move this listener + the billingClient
                        // initiation into the SubscriptionsPlugin.java, then pass it into this implementation class so we can still access the
                        // billingClient.
                        notifyListeners("ANDROID-PURCHASE-RESPONSE", response);
                    });
                } else {
                    response.put("successful", false);
                    notifyListeners("ANDROID-PURCHASE-RESPONSE", response);
                }

            }
        } else {
            response.put("successful", false);
            notifyListeners("ANDROID-PURCHASE-RESPONSE", response);
        }
    };

    @Override
    public void load() {
        this.billingClient = BillingClient.newBuilder(getContext())
                .setListener(purchasesUpdatedListener)

                .enablePendingPurchases(
                        PendingPurchasesParams
                                .newBuilder()
                                .enableOneTimeProducts()
                                .enablePrepaidPlans()
                                .build()
                )
                .build();

        this.implementation = new InAppPurchase(this, billingClient);
    }

    @PluginMethod
    public void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
    }

    @PluginMethod
    public void getProductDetails(PluginCall call) {

        String productId = call.getString("productId");

        if (productId == null) {
            call.reject("Must provide a productID");
        }

        implementation.getProductDetails(productId, call);
    }

    @PluginMethod
    public void purchase(PluginCall call) {

        String identifier = call.getString("productId");
        String productType = call.getString("productType");
        JSObject payload = call.getObject("developerPayload");

        String accountId = "";
        String profileId = "";
        if(payload != null) {
            accountId = payload.getString("serviceType");
            profileId = payload.getString("serviceId");
        }


        String productId;
        String basePlan = null;

        if (identifier.contains("@")) {
            String[] parts = identifier.split("@");
            productId = parts[0];
            basePlan = parts[1];
        } else {
            productId = identifier;
        }

        if(productId == null) {
            call.reject("Must provide a productID");
        }

        implementation.purchaseProduct(productId, basePlan, productType, accountId, profileId, call);
        

    }
}