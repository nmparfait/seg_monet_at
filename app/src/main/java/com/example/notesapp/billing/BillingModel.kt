package com.example.notesapp.billing

import com.android.billingclient.api.SkuDetails

class BillingModel(var sku : String, var descricao : String?, var preco : String?) {
    var skuDetails : SkuDetails? = null
}