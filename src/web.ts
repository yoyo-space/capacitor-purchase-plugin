import { WebPlugin } from '@capacitor/core';

import type {
  AppleTransactionResult,
  InAppPurchasePlugin,
  InAppPurchaseRequest,
  InAppPurchaseResult,
} from './definitions';

export class InAppPurchaseWeb extends WebPlugin implements InAppPurchasePlugin {
  initialize(): Promise<void> {
    console.log('initialize() called');
    return Promise.resolve();
  }
  getProducts(): Promise<void> {
    console.log('getProducts() called');
    return Promise.resolve();
  }
  purchase(request: InAppPurchaseRequest): Promise<InAppPurchaseResult | AppleTransactionResult> {
    console.log('purchase() called with productId:', request.productId);
    return Promise.resolve({} as InAppPurchaseResult);
  }
  registerProducts(products: { products: string[] }): Promise<void> {
    console.log('registerProducts() called with products:', products);
    return Promise.resolve();
  }
  canSubscribe(product: { productId: string }): Promise<void> {
    console.log('canSubscribe() called with productId:', product.productId);
    return Promise.resolve();
  }
  addListener(eventName: 'ANDROID-PURCHASE-RESPONSE', listenerFunc: (response: any) => void): Promise<any> {
    console.log('addListener() called with eventName:', eventName, 'and listenerFunc:', listenerFunc);
    return Promise.resolve();
  }
}
