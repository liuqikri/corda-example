package com.ehkd.corda.util;

import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.testing.node.MockNetwork;
import net.corda.testing.node.MockNetworkParameters;
import net.corda.testing.node.StartedMockNode;
import net.corda.testing.node.TestCordapp;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Kris Lau
 * @date 2023/3/22
 */
public class TestParties {
    boolean isSetup = false;
    public MockNetwork net;
    public StartedMockNode partyA;
    public StartedMockNode partyB;
    public Party notary;
    List<StartedMockNode> nodes = new ArrayList<>();
    CordaX500Name ALICE_NAME = new CordaX500Name("Alice Corp", "Madrid", "ES");
    CordaX500Name BOB_NAME = new CordaX500Name("Bob Plc", "Rome", "IT");

    public TestParties() {
    }

    public void setup() {
        ArrayList<TestCordapp> list = new ArrayList<>();
        list.add(TestCordapp.findCordapp("com.ehkd.corda"));
        net = new MockNetwork(
                new MockNetworkParameters(
                        list
                ));

        partyA = net.createPartyNode(ALICE_NAME);
        partyB = net.createPartyNode(BOB_NAME);
        notary = net.getDefaultNotaryIdentity();
        nodes.add(partyA);
        nodes.add(partyB);
        net.runNetwork();
        isSetup = true;
    }

    public void tearDown() {
        if(isSetup) {
            net.stopNodes();
        }
    }
}
