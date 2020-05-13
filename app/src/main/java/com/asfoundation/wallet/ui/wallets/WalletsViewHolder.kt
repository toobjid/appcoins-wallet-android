package com.asfoundation.wallet.ui.wallets

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.asf.wallet.R
import com.asfoundation.wallet.util.CurrencyFormatUtils
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.other_wallet_card.view.*

class WalletsViewHolder(private val context: Context, itemView: View,
                        private val uiEventListener: PublishSubject<String>,
                        private val currencyFormatUtils: CurrencyFormatUtils) :
    RecyclerView.ViewHolder(itemView) {

  @SuppressLint("SetTextI18n")
  fun bind(item: WalletBalance) {
    itemView.active_wallet_address.text = item.walletAddress
    itemView.wallet_balance.text = context.getString(
        R.string.wallets_2nd_view_balance_title) + " " + item.balance.symbol + currencyFormatUtils.formatCurrency(
        item.balance.amount)
    itemView.setOnClickListener { uiEventListener.onNext(item.walletAddress) }
  }
}