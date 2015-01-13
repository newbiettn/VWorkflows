/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vrl.workflow;

import eu.mihosoft.vrl.workflow.skin.ConnectionSkin;
import eu.mihosoft.vrl.workflow.skin.Skin;
import eu.mihosoft.vrl.workflow.skin.SkinFactory;
import eu.mihosoft.vrl.workflow.skin.VNodeSkin;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class SkinTest {

    @Test
    public void nodeSkinTest() {
        VFlow flow = FlowFactory.newFlow();
        VNodeSkinFactoryStub skinFactory = new VNodeSkinFactoryStub();
        flow.setSkinFactories(skinFactory);

        flow.setVisible(true);
        VNode n1 = flow.newNode();

        Assert.assertTrue("Skin for n1 must be present.",
                skinFactory.getNodeSkins().get(n1) != null);

        Assert.assertTrue("Skin must be skin of n1.",
                skinFactory.getNodeSkins().get(n1).getModel() == n1);

        VFlow sf1 = flow.newSubFlow();

        Assert.assertTrue("Skin for sf1 must be present.",
                skinFactory.getNodeSkins().get(sf1.getModel()) != null);

        Optional<VNodeSkinFactoryStub> sfSkinFactry
                = skinFactory.getChildFactories().values().stream().findFirst();

        Assert.assertTrue("Child factory must exist.", sfSkinFactry.isPresent());

        VNode sn1 = sf1.newNode();

        sf1.setVisible(true);
        Assert.assertTrue("Skin for sn1 must be present.",
                 sfSkinFactry.get().getNodeSkins().get(sn1) != null);
        
        Assert.assertTrue("Skin must be skin of n1.",
                sfSkinFactry.get().getNodeSkins().get(sn1).getModel() == sn1);
    }
}

final class VNodeSkinStub implements VNodeSkin<VNode> {

    private final ObjectProperty<VNode> modelProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<VFlow> flowProperty = new SimpleObjectProperty<>();
    private final SkinFactory skinFactory;

    public VNodeSkinStub(SkinFactory skinFactory, VNode n, VFlow flow) {
        this.skinFactory = skinFactory;
        setController(flow);
        setModel(n);
    }

    @Override
    public void add() {
        //
    }

    @Override
    public void remove() {
        //
    }

    @Override
    public void setModel(VNode model) {
        modelProperty().set(model);
    }

    @Override
    public VNode getModel() {
        return modelProperty().get();
    }

    @Override
    public ObjectProperty<VNode> modelProperty() {
        return modelProperty;
    }

    @Override
    public VFlow getController() {
        return flowProperty.get();
    }

    @Override
    public void setController(VFlow flow) {
        flowProperty.set(flow);
    }

    @Override
    public SkinFactory getSkinFactory() {
        return skinFactory;
    }
}

final class ConnectionSkinStub implements ConnectionSkin<Connection> {

    private final ObjectProperty<Connector> senderProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Connector> receiverProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<Connection> modelProperty = new SimpleObjectProperty<>();
    private final ObjectProperty<VFlow> flowProperty = new SimpleObjectProperty<>();
    private final SkinFactory skinFactory;

    public ConnectionSkinStub(SkinFactory skinFactory, Connection c, VFlow flow) {
        this.skinFactory = skinFactory;
        setModel(c);
        setController(flow);
    }

    @Override
    public Connector getSender() {
        return senderProperty().get();
    }

    @Override
    public void setSender(Connector c) {
        senderProperty().set(c);
    }

    @Override
    public ObjectProperty<Connector> senderProperty() {
        return senderProperty;
    }

    @Override
    public Connector getReceiver() {
        return receiverProperty().get();
    }

    @Override
    public void setReceiver(Connector c) {
        receiverProperty().set(c);
    }

    @Override
    public ObjectProperty<Connector> receiverProperty() {
        return receiverProperty;
    }

    @Override
    public void add() {
        //
    }

    @Override
    public void remove() {
        //
    }

    @Override
    public void setModel(Connection model) {
        modelProperty().set(model);
    }

    @Override
    public Connection getModel() {
        return modelProperty().get();
    }

    @Override
    public ObjectProperty<Connection> modelProperty() {
        return modelProperty;
    }

    @Override
    public VFlow getController() {
        return flowProperty.get();
    }

    @Override
    public void setController(VFlow flow) {
        flowProperty.set(flow);
    }

    @Override
    public SkinFactory getSkinFactory() {
        return skinFactory;
    }

}

class VNodeSkinFactoryStub implements SkinFactory<ConnectionSkinStub, VNodeSkinStub> {

    private final Map<VNode, VNodeSkinStub> nodeSkins = new HashMap<>();
    private final Map<Connection, ConnectionSkinStub> connectionSkins = new HashMap<>();

    private final Map<VFlow, VNodeSkinFactoryStub> childFactories = new HashMap<>();

    private SkinFactory<ConnectionSkinStub, VNodeSkinStub> parentFactory;

    VNodeSkinFactoryStub() {
        //
    }

    @Override
    public VNodeSkin createSkin(VNode n, VFlow controller) {
        VNodeSkinStub skin = new VNodeSkinStub(this, n, controller);

        System.out.println("N: " + n.getId());

        getNodeSkins().put(n, skin);

        return skin;
    }

    @Override
    public SkinFactory<ConnectionSkinStub, VNodeSkinStub> createChild(Skin parent) {
        VNodeSkinFactoryStub sf = new VNodeSkinFactoryStub();

        getChildFactories().put(parent.getController(), sf);

        return sf;
    }

    @Override
    public SkinFactory<ConnectionSkinStub, VNodeSkinStub> getParent() {
        return parentFactory;
    }

    @Override
    public ConnectionSkin createSkin(Connection c, VFlow flow, String type) {
        ConnectionSkinStub skin = new ConnectionSkinStub(this, c, flow);

        getConnectionSkins().put(c, skin);

        return skin;
    }

    /**
     * @return the nodeSkins
     */
    public Map<VNode, VNodeSkinStub> getNodeSkins() {
        return nodeSkins;
    }

    /**
     * @return the connectionSkins
     */
    public Map<Connection, ConnectionSkinStub> getConnectionSkins() {
        return connectionSkins;
    }

    /**
     * @return the childFactories
     */
    public Map<VFlow, VNodeSkinFactoryStub> getChildFactories() {
        return childFactories;
    }

}