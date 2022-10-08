package com.example.notesapp.billing

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.android.billingclient.api.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BillingClass
    (private var context: AppCompatActivity) : BillingClientStateListener,
    SkuDetailsResponseListener, PurchasesUpdatedListener {


    val produtos: MutableList<BillingModel> = ArrayList()
    var clienteInApp: BillingClient
    val skuList = ArrayList<String>()


    var result: BillingResult? = null


    init {
        produtos.add(
            BillingModel(
                "android.test.purchased",
                "Versão premium do aplicativo inNotes App",
                null
            )
        )
        clienteInApp = BillingClient
            .newBuilder(context)
            .enablePendingPurchases()
            .setListener(this)
            .build()
        clienteInApp.startConnection(this)
    }

    //-------------------------------------------------------

    override fun onBillingSetupFinished(billingResult: BillingResult?) {

        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {


            for (produto in produtos) {
                skuList.add(produto.sku)
            }
            val params = SkuDetailsParams
                .newBuilder()
                .setSkusList(skuList)
                .setType(BillingClient.SkuType.INAPP)
                .build()
            clienteInApp.querySkuDetailsAsync(params, this)
        }
    }

    override fun onBillingServiceDisconnected() {
    }

    override fun onSkuDetailsResponse(
        billingResult: BillingResult?,
        skuDetailsList: MutableList<SkuDetails>?
    ) {
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK
            && skuDetailsList != null
            && skuDetailsList.size > 0
        ) {

            for (produto in produtos) {

                for (product in skuDetailsList) {

                    if (produto.sku.equals(product.sku)) {

                        produto.descricao = product.description
                        produto.preco = product.price
                        produto.skuDetails = product
                    }
                }
            }

        }
    }

    //------------------------------------------------------------------------

    fun efetuarCompra(produto: BillingModel) {

        Log.i("AT", "Efetuando uma compra")
        val params = BillingFlowParams
            .newBuilder()
            .setSkuDetails(produto.skuDetails)
            .build()
        clienteInApp.launchBillingFlow(context, params)
    }

    //-------------------------------------------------------------------------

    override fun onPurchasesUpdated(
        billingResult: BillingResult?,
        purchaseList: MutableList<Purchase>?
    ) {
        if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK
            && purchaseList != null
            && purchaseList.size > 0
        ) {

            Log.i("AT", "Compra efetuada")

            for (purchase in purchaseList) {


                GlobalScope.launch(Dispatchers.IO) {

                    handlePurchase(purchase)
                }

            }
        } else if (billingResult?.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.i("AT", "Usuário cancelou a compra")
        }
    }

    suspend fun handlePurchase(purchase: Purchase) {

        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {

            Log.i("AT", "Compra Confirmada ${purchase.purchaseState}")

            //------------

            if (!purchase.isAcknowledged) {

                val params = AcknowledgePurchaseParams
                    .newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                result = withContext(Dispatchers.IO) {
                    clienteInApp.acknowledgePurchase(params)
                }
            }
        }
    }

    fun fecharLoja() {

        Log.i("AT", "Fechando a Loja")
        clienteInApp.endConnection()

    }

}