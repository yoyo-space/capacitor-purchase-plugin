import { InAppPurchase } from 'capacitor-purchase-plugin';

window.testEcho = () => {
    const inputValue = document.getElementById("echoInput").value;
    InAppPurchase.echo({ value: inputValue })
}
