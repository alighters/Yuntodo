package com.oldwei.yifavor.activity;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;

import com.oldwei.yifavor.R;
import com.oldwei.yifavor.model.LinkModel;
import com.oldwei.yifavor.utils.JSONUtils;

public class CardActivity extends Activity {

    private CardListView mCardListView;

    private CardArrayAdapter mCardArrayAdapter;

    private List<Card> mCards = new ArrayList<Card>();

    @Override
    protected void onCreate(Bundle savedInstnceState) {
        super.onCreate(savedInstnceState);
        setContentView(R.layout.card_activity);
        initView();
        setData();
        addData(); 
    }

    private void initView() {
        mCardListView = (CardListView) findViewById(R.id.myList);
    }

    private void setData() {
        mCardArrayAdapter = new CardArrayAdapter(this, mCards);
        if (mCardListView != null) {
            mCardListView.setAdapter(mCardArrayAdapter);
        }
    }

    private void addData() {
        List<LinkModel> links = JSONUtils.loadLinkList();
        Card card;
        for (LinkModel linkModel : links) {
            card = new Card(this);
            card.setTitle(linkModel.getTitle());
            card.setExpanded(true);
            card.setCheckable(true);
            mCards.add(card);
        }
        mCardArrayAdapter.notifyDataSetChanged();
    }
}
