# capacitor-purchase-plugin

Capacitor In-App Purchases plugin

## Install

```bash
npm install capacitor-purchase-plugin
npx cap sync
```

## API

<docgen-index>

* [`initialize()`](#initialize)
* [`getProducts()`](#getproducts)
* [`purchase(...)`](#purchase)
* [`registerProducts(...)`](#registerproducts)
* [`canSubscribe(...)`](#cansubscribe)
* [`addListener('ANDROID-PURCHASE-RESPONSE', ...)`](#addlistenerandroid-purchase-response-)
* [`removeAllListeners()`](#removealllisteners)
* [Interfaces](#interfaces)
* [Enums](#enums)

</docgen-index>

<docgen-api>
<!--Update the source file JSDoc comments and rerun docgen to update the docs below-->

### initialize()

```typescript
initialize() => Promise<void>
```

--------------------


### getProducts()

```typescript
getProducts() => Promise<void>
```

--------------------


### purchase(...)

```typescript
purchase(request: InAppPurchaseRequest) => Promise<InAppPurchaseResult | AppleTransactionResult>
```

| Param         | Type                                                                  |
| ------------- | --------------------------------------------------------------------- |
| **`request`** | <code><a href="#inapppurchaserequest">InAppPurchaseRequest</a></code> |

**Returns:** <code>Promise&lt;<a href="#inapppurchaseresult">InAppPurchaseResult</a> | <a href="#appletransactionresult">AppleTransactionResult</a>&gt;</code>

--------------------


### registerProducts(...)

```typescript
registerProducts(products: { products: string[]; }) => Promise<void>
```

| Param          | Type                                 |
| -------------- | ------------------------------------ |
| **`products`** | <code>{ products: string[]; }</code> |

--------------------


### canSubscribe(...)

```typescript
canSubscribe(product: { productId: string; }) => Promise<void>
```

| Param         | Type                                |
| ------------- | ----------------------------------- |
| **`product`** | <code>{ productId: string; }</code> |

--------------------


### addListener('ANDROID-PURCHASE-RESPONSE', ...)

```typescript
addListener(eventName: 'ANDROID-PURCHASE-RESPONSE', listenerFunc: (response: PurchaseEvent) => void) => Promise<PluginListenerHandle>
```

| Param              | Type                                                                           |
| ------------------ | ------------------------------------------------------------------------------ |
| **`eventName`**    | <code>'ANDROID-PURCHASE-RESPONSE'</code>                                       |
| **`listenerFunc`** | <code>(response: <a href="#purchaseevent">PurchaseEvent</a>) =&gt; void</code> |

**Returns:** <code>Promise&lt;<a href="#pluginlistenerhandle">PluginListenerHandle</a>&gt;</code>

--------------------


### removeAllListeners()

```typescript
removeAllListeners() => Promise<void>
```

--------------------


### Interfaces


#### InAppPurchaseResult

| Prop               | Type                |
| ------------------ | ------------------- |
| **`id`**           | <code>string</code> |
| **`originalID`**   | <code>string</code> |
| **`productID`**    | <code>string</code> |
| **`price`**        | <code>number</code> |
| **`purchaseDate`** | <code>number</code> |
| **`currency`**     | <code>string</code> |


#### AppleTransactionResult

| Prop                       | Type                 |
| -------------------------- | -------------------- |
| **`originalPurchaseDate`** | <code>number</code>  |
| **`expirationDate`**       | <code>number</code>  |
| **`isUpgraded`**           | <code>boolean</code> |
| **`offerID`**              | <code>string</code>  |
| **`revocationDate`**       | <code>number</code>  |
| **`revocationReason`**     | <code>number</code>  |


#### InAppPurchaseRequest

| Prop                   | Type                                                     |
| ---------------------- | -------------------------------------------------------- |
| **`productId`**        | <code>string</code>                                      |
| **`productType`**      | <code><a href="#producttype">ProductType</a></code>      |
| **`developerPayload`** | <code>{ serviceType: string; serviceId: string; }</code> |


#### PluginListenerHandle

| Prop         | Type                                      |
| ------------ | ----------------------------------------- |
| **`remove`** | <code>() =&gt; Promise&lt;void&gt;</code> |


#### PurchaseEvent

| Prop              | Type                |
| ----------------- | ------------------- |
| **`id`**          | <code>string</code> |
| **`serviceType`** | <code>string</code> |
| **`serviceId`**   | <code>string</code> |
| **`productId`**   | <code>string</code> |
| **`payload`**     | <code>string</code> |
| **`token`**       | <code>string</code> |


### Enums


#### ProductType

| Members            | Value                       |
| ------------------ | --------------------------- |
| **`ONE_TIME`**     | <code>'ONE_TIME'</code>     |
| **`SUBSCRIPTION`** | <code>'SUBSCRIPTION'</code> |

</docgen-api>
