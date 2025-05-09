package space.yoyo.capacitor.purchase;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.ProductDetailsResponseListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchaseHistoryRecord;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchaseHistoryParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.getcapacitor.PluginCall;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class InAppPurchase {

    private final Activity activity;
    public Context context;
    private final BillingClient billingClient;
    private int billingClientIsConnected = 0;

    InAppPurchase(InAppPurchasePlugin plugin, BillingClient billingClient) {
        this.billingClient = billingClient;
        this.billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    billingClientIsConnected = 1;
                } else {
                    billingClientIsConnected = billingResult.getResponseCode();
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
        this.activity = plugin.getActivity();
        this.context = plugin.getContext();
    }

    public String echo(String value) {
        Log.i("Echo", value);
        return value;
    }

    public void getProductDetails(String identifier, PluginCall call) {
        JSObject response = new JSObject();

        if(billingClientIsConnected == 1) {
            QueryProductDetailsParams.Product productToFind = QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(identifier)
                    .build();

            QueryProductDetailsParams queryProductDetailsParams =
                    QueryProductDetailsParams.newBuilder()
                            .setProductList(List.of(productToFind))
                            .build();

            billingClient.queryProductDetailsAsync(
                    queryProductDetailsParams,
                    (billingResult, productDetailsList) -> {

                        try {

                            ProductDetails productDetails = productDetailsList.get(0);
                            String productId = productDetails.getProductId();
                            String title = productDetails.getTitle();
                            String desc = productDetails.getDescription();
                            Log.i("productIdentifier", productId);
                            Log.i("displayName", title);
                            Log.i("desc", desc);

                            List<ProductDetails.SubscriptionOfferDetails> subscriptionOfferDetails = productDetails.getSubscriptionOfferDetails();

                            String price = Objects.requireNonNull(subscriptionOfferDetails)
                                    .get(0).getPricingPhases()
                                    .getPricingPhaseList()
                                    .get(0)
                                    .getFormattedPrice();

                            JSObject data = new JSObject();
                            data.put("productIdentifier", productId);
                            data.put("displayName", title);
                            data.put("description", desc);
                            data.put("price", price);

                            response.put("responseCode", 0);
                            response.put("responseMessage", "Successfully found the product details for given productIdentifier");
                            response.put("data", data);

                        } catch (Exception e) {
                            Log.e("Err", e.toString());
                            response.put("responseCode", 1);
                            response.put("responseMessage", "Could not find a product matching the given productIdentifier");
                        }

                        call.resolve(response);
                    }
            );
        }
    }


    public void purchaseProduct(String productIdentifier, String basePlan, String productType, String accountId, String profileId, PluginCall call) {

        JSObject response = new JSObject();

        if (billingClientIsConnected == 1) {
            ArrayList<QueryProductDetailsParams.Product> productList = new ArrayList<>();

            QueryProductDetailsParams.Product productToFind = QueryProductDetailsParams.Product.newBuilder()
                    .setProductType(productType.equals("ONE_TIME") ? BillingClient.ProductType.INAPP : BillingClient.ProductType.SUBS)
                    .setProductId(productIdentifier)
                    .build();

            productList.add(productToFind);


            QueryProductDetailsParams queryProductDetailsParams =
                    QueryProductDetailsParams.newBuilder()
                            .setProductList(productList)
                            .build();

            billingClient.queryProductDetailsAsync(
                    queryProductDetailsParams,
                    (billingResult1, productDetailsList) -> {
                        BillingFlowParams billingFlowParams;
                        BillingResult result;
                        try {
                            ProductDetails productDetails = productDetailsList.get(0);
                            if(productDetails.getProductType().equals(BillingClient.ProductType.SUBS)) {

                                if(productDetails.getSubscriptionOfferDetails() == null) return;
                                ProductDetails.SubscriptionOfferDetails offerDetails = productDetails.getSubscriptionOfferDetails().get(0);
                                for(ProductDetails.SubscriptionOfferDetails offer : productDetails.getSubscriptionOfferDetails()){
                                    if(basePlan != null && offer.getBasePlanId().equals(basePlan)){
                                        offerDetails = offer;
                                    }
                                }

                                billingFlowParams = BillingFlowParams.newBuilder()
                                        .setProductDetailsParamsList(
                                                List.of(
                                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                                                .setProductDetails(productDetails)
                                                                .setOfferToken(Objects.requireNonNull(offerDetails.getOfferToken()))
                                                                .build()
                                                )
                                        )
                                        .setObfuscatedAccountId(accountId)
                                        .setObfuscatedProfileId(profileId)
                                        .build();
                                result = billingClient.launchBillingFlow(this.activity, billingFlowParams);
                            } else {
                                billingFlowParams = BillingFlowParams.newBuilder()
                                        .setProductDetailsParamsList(
                                                List.of(
                                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                                                .setProductDetails(productDetails)
                                                                .build()
                                                )
                                        )
                                        .setObfuscatedAccountId(accountId)
                                        .setObfuscatedProfileId(profileId)
                                        .build();
                                result = billingClient.launchBillingFlow(this.activity, billingFlowParams);
                            }


                            Log.i("RESULT", result.toString());

                            response.put("responseCode", 0);
                            response.put("responseMessage", "Successfully opened native popover");

                        } catch (Exception e) {
                            Logger.error(e.getMessage());
                            response.put("responseCode", 1);
                            response.put("responseMessage", "Failed to open native popover");
                        }

                        call.resolve(response);

                    });
        }

    }

    public void getLatestTransaction(String productIdentifier, PluginCall call) {

        JSObject response = new JSObject();

        if (billingClientIsConnected == 1) {

            QueryPurchaseHistoryParams queryPurchaseHistoryParams =
                    QueryPurchaseHistoryParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build();


            billingClient.queryPurchaseHistoryAsync(queryPurchaseHistoryParams, (BillingResult billingResult, List<PurchaseHistoryRecord> list) -> {

                // Try to loop through the list until we find a purchase history record associated with the passed in productIdentifier.
                // If we do, then set found to true to break out of the loop, then compile a response with necessary data. Otherwise compile
                // a response saying that the there were not transactions for the given productIdentifier.
                int i = 0;
                boolean found = false;
                while (list != null && (i < list.size() && !found)) {
                    try {

                        JSObject currentPurchaseHistoryRecord = new JSObject(list.get(i).getOriginalJson());
                        Log.i("PurchaseHistory", currentPurchaseHistoryRecord.toString());

                        if (currentPurchaseHistoryRecord.get("productId").equals(productIdentifier)) {

                            found = true;

                            JSObject data = new JSObject();

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTimeInMillis(Long.parseLong((currentPurchaseHistoryRecord.get("purchaseTime").toString())));
                            String orderId = currentPurchaseHistoryRecord.optString("orderId", "");  // Usamos optString para obtener un valor por defecto si la clave no existe
                            data.put("productIdentifier", currentPurchaseHistoryRecord.get("productId"));
                            data.put("originalId", orderId);
                            data.put("transactionId", orderId);
                            data.put("developerPayload",currentPurchaseHistoryRecord.optString("developerPayload", ""));  // Usamos optString para obtener un valor por defecto si la clave no existe
                            data.put("purchaseToken", currentPurchaseHistoryRecord.get("purchaseToken").toString());

                            response.put("responseCode", 0);
                            response.put("responseMessage", "Successfully found the latest transaction matching given productIdentifier");
                            response.put("data", data);
                        }
                    } catch (Exception e) {
                        Logger.error(e.getMessage());
                    }

                    i++;

                }

                // If after looping through the list of purchase history records, no records are found to be associated with
                // the given product identifier, return a response saying no transactions found
                if (!found) {
                    response.put("responseCode", 3);
                    response.put("responseMessage", "No transaction for given productIdentifier, or it could not be verified");
                }

                call.resolve(response);

            });

        }

    }


    public void getCurrentEntitlements(PluginCall call) {

        JSObject response = new JSObject();

        if (billingClientIsConnected == 1) {

            QueryPurchasesParams queryPurchasesParams =
                    QueryPurchasesParams.newBuilder()
                            .setProductType(BillingClient.ProductType.SUBS)
                            .build();

            billingClient.queryPurchasesAsync(
                    queryPurchasesParams,
                    (billingResult, purchaseList) -> {

                        try {

                            int amountOfPurchases = purchaseList.size();

                            if (amountOfPurchases > 0) {

                                ArrayList<JSObject> entitlements = new ArrayList<>();
                                for (int i = 0; i < purchaseList.size(); i++) {

                                    Purchase currentPurchase = purchaseList.get(i);

                                    String orderId = currentPurchase.getOrderId();

                                    String dateFormat = "dd-MM-yyyy hh:mm";
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.getDefault());
                                    Calendar calendar = Calendar.getInstance();
                                    calendar.setTimeInMillis(Long.parseLong((String.valueOf(currentPurchase.getPurchaseTime()))));

                                    entitlements.add(
                                            new JSObject()
                                                    .put("productIdentifier", currentPurchase.getProducts().get(0))
                                                    .put("originalStartDate", simpleDateFormat.format(calendar.getTime()))
                                                    .put("originalId", orderId)
                                                    .put("transactionId", orderId)
                                                    .put("purchaseToken", currentPurchase.getPurchaseToken())
                                    );
                                }

                                response.put("responseCode", 0);
                                response.put("responseMessage", "Successfully found all entitlements across all product types");
                                response.put("data", entitlements);


                            } else {
                                Log.i("No Purchases", "No active subscriptions found");
                                response.put("responseCode", 1);
                                response.put("responseMessage", "No entitlements were found");
                            }


                            call.resolve(response);

                        } catch (Exception e) {
                            Log.e("Error", e.toString());
                            response.put("responseCode", 2);
                            response.put("responseMessage", e.toString());
                        }

                        call.resolve(response);

                    }
            );

        }

    }
}
