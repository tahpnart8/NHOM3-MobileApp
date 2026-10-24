package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.Listing;
import com.nhom3.pubgapp.model.enums.SortOrder;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SearchViewModelTest {

    @Test
    public void testIsPriceRangeValid_unbounded() {
        assertTrue(SearchViewModel.isPriceRangeValid(0, 0));
        assertTrue(SearchViewModel.isPriceRangeValid(100000, 0));
        assertTrue(SearchViewModel.isPriceRangeValid(0, 500000));
    }

    @Test
    public void testIsPriceRangeValid_validRange() {
        assertTrue(SearchViewModel.isPriceRangeValid(100000, 100000));
        assertTrue(SearchViewModel.isPriceRangeValid(100000, 500000));
    }

    @Test
    public void testIsPriceRangeValid_invalidRange() {
        assertFalse(SearchViewModel.isPriceRangeValid(500000, 100000));
    }

    @Test
    public void testSortResults_nullList() {
        List<Listing> sorted = SearchViewModel.sortResults(null, SortOrder.NEWEST);
        assertTrue(sorted.isEmpty());
    }

    @Test
    public void testSortResults_priceAsc() {
        List<Listing> list = new ArrayList<Listing>();
        Listing l1 = new Listing();
        l1.setPrice(300000);
        Listing l2 = new Listing();
        l2.setPrice(100000);
        Listing l3 = new Listing();
        l3.setPrice(200000);
        list.add(l1);
        list.add(l2);
        list.add(l3);

        List<Listing> sorted = SearchViewModel.sortResults(list, SortOrder.PRICE_ASC);
        assertEquals(3, sorted.size());
        assertEquals(100000, sorted.get(0).getPrice());
        assertEquals(200000, sorted.get(1).getPrice());
        assertEquals(300000, sorted.get(2).getPrice());
    }

    @Test
    public void testSortResults_priceDesc() {
        List<Listing> list = new ArrayList<Listing>();
        Listing l1 = new Listing();
        l1.setPrice(100000);
        Listing l2 = new Listing();
        l2.setPrice(500000);
        Listing l3 = new Listing();
        l3.setPrice(300000);
        list.add(l1);
        list.add(l2);
        list.add(l3);

        List<Listing> sorted = SearchViewModel.sortResults(list, SortOrder.PRICE_DESC);
        assertEquals(3, sorted.size());
        assertEquals(500000, sorted.get(0).getPrice());
        assertEquals(300000, sorted.get(1).getPrice());
        assertEquals(100000, sorted.get(2).getPrice());
    }

    @Test
    public void testSortResults_newest() {
        List<Listing> list = new ArrayList<Listing>();
        Listing l1 = new Listing();
        l1.setCreatedAt(new Timestamp(1000, 0));
        Listing l2 = new Listing();
        l2.setCreatedAt(new Timestamp(3000, 0));
        Listing l3 = new Listing();
        l3.setCreatedAt(new Timestamp(2000, 0));
        list.add(l1);
        list.add(l2);
        list.add(l3);

        List<Listing> sorted = SearchViewModel.sortResults(list, SortOrder.NEWEST);
        assertEquals(3, sorted.size());
        assertEquals(3000, sorted.get(0).getCreatedAt().getSeconds());
        assertEquals(2000, sorted.get(1).getCreatedAt().getSeconds());
        assertEquals(1000, sorted.get(2).getCreatedAt().getSeconds());
    }
}
