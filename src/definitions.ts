import type { PluginListenerHandle } from '@capacitor/core';

export interface InAppPurchasePlugin {
  initialize(): Promise<void>;
  getProducts(): Promise<void>;
  purchase(request: InAppPurchaseRequest): Promise<InAppPurchaseResult | AppleTransactionResult>;
  registerProducts(products: { products: string[] }): Promise<void>;
  canSubscribe(product: { productId: string }): Promise<void>;
  addListener(
    eventName: 'ANDROID-PURCHASE-RESPONSE',
    listenerFunc: (response: PurchaseEvent) => void,
  ): Promise<PluginListenerHandle>;

  removeAllListeners(): Promise<void>;
}

export enum ProductType {
  ONE_TIME = 'ONE_TIME',
  SUBSCRIPTION = 'SUBSCRIPTION',
}

export interface InAppPurchaseRequest {
  productId: string;
  productType?: ProductType;
  developerPayload?: {
    serviceType: string;
    serviceId: string;
  };
}

export interface PurchaseEvent {
  id: string;
  serviceType: string;
  serviceId: string;
  productId: string;
  payload: string;
  token: string;
}

export interface InAppPurchaseResult {
  id: string;
  originalID: string;
  productID: string;
  price: number;
  purchaseDate: number;
  currency: string;
}

export interface AppleTransactionResult extends InAppPurchaseResult {
  originalPurchaseDate: number;
  expirationDate?: number;
  isUpgraded: boolean;
  offerID?: string;
  revocationDate?: number;
  revocationReason?: number;
}
