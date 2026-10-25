package com.nhom3.pubgapp.viewmodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.firebase.Timestamp;
import com.nhom3.pubgapp.model.Complaint;
import com.nhom3.pubgapp.model.Deal;
import com.nhom3.pubgapp.model.enums.Priority;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class AdminComplaintViewModelTest {

    @Test
    public void highPriorityComesFirst() {
        List<Complaint> source = new ArrayList<Complaint>();
        source.add(complaint("low", Priority.LOW, 300));
        source.add(complaint("high", Priority.HIGH, 100));
        source.add(complaint("medium", Priority.MEDIUM, 200));
        List<Complaint> sorted = AdminComplaintViewModel.sortForAdmin(source);
        assertEquals("high", sorted.get(0).getDealId());
        assertEquals("medium", sorted.get(1).getDealId());
        assertEquals("low", sorted.get(2).getDealId());
    }

    @Test
    public void samePriorityNewestFirst() {
        List<Complaint> source = new ArrayList<Complaint>();
        source.add(complaint("older", Priority.HIGH, 100));
        source.add(complaint("newer", Priority.HIGH, 200));
        List<Complaint> sorted = AdminComplaintViewModel.sortForAdmin(source);
        assertEquals("newer", sorted.get(0).getDealId());
        assertEquals("older", sorted.get(1).getDealId());
    }

    @Test
    public void missingPriorityCountsAsMedium() {
        List<Complaint> source = new ArrayList<Complaint>();
        source.add(complaint("low", Priority.LOW, 300));
        source.add(complaint("none", null, 100));
        List<Complaint> sorted = AdminComplaintViewModel.sortForAdmin(source);
        assertEquals("none", sorted.get(0).getDealId());
    }

    @Test
    public void nullListGivesEmptyList() {
        assertTrue(AdminComplaintViewModel.sortForAdmin(null).isEmpty());
    }

    @Test
    public void conversationIdIsListingThenBuyer() {
        Deal deal = new Deal();
        deal.setListingId("listing-1");
        deal.setBuyerId("buyer-1");
        assertEquals("listing-1_buyer-1", AdminComplaintViewModel.conversationIdOf(deal));
    }

    private static Complaint complaint(String dealId, Priority priority, long seconds) {
        Complaint complaint = new Complaint();
        complaint.setDealId(dealId);
        complaint.setPriority(priority);
        complaint.setCreatedAt(new Timestamp(seconds, 0));
        return complaint;
    }
}
