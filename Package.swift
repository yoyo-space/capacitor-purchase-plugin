// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "CapacitorPurchasePlugin",
    platforms: [.iOS(.v14)],
    products: [
        .library(
            name: "CapacitorPurchasePlugin",
            targets: ["InAppPurchasePlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", from: "7.0.0")
    ],
    targets: [
        .target(
            name: "InAppPurchasePlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/InAppPurchasePlugin"),
        .testTarget(
            name: "InAppPurchasePluginTests",
            dependencies: ["InAppPurchasePlugin"],
            path: "ios/Tests/InAppPurchasePluginTests")
    ]
)