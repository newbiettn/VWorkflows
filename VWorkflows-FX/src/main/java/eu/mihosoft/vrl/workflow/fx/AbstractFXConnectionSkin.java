/*
 * Copyright 2012-2016 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * Please cite the following publication(s):
 *
 * M. Hoffer, C.Poliwoda, G.Wittum. Visual Reflection Library -
 * A Framework for Declarative GUI Programming on the Java Platform.
 * Computing and Visualization in Science, 2011, in press.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
 */
package eu.mihosoft.vrl.workflow.fx;

import eu.mihosoft.vrl.workflow.Connection;
import eu.mihosoft.vrl.workflow.Connector;
import eu.mihosoft.vrl.workflow.VFlow;
import javafx.animation.PathTransition;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public abstract class AbstractFXConnectionSkin implements FXConnectionSkin {

    // -- properties
    protected final ObjectProperty<Connector> senderProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<Connector> receiverProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<Connection> modelProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<Parent> parentProperty = new SimpleObjectProperty<>();
    protected final ObjectProperty<String> nameProperty = new SimpleObjectProperty<>();

    public String getName() {
        return nameProperty.get();
    }

    public void setName(String nameProperty) {
        this.nameProperty.set(nameProperty);
    }


    // -- properties

    // -- mutable fields
    protected Path connectionPath;
    protected Text connectionText;
    protected CubicCurve invisibleCurve;
    protected Circle receiverConnectorUI;
    protected VFlow controller;
    protected ConnectorShape senderShape;
    protected ConnectorShape receiverShape;
    protected ConnectionListener connectionListener;

    private boolean initialized;
    // -- mutable fields

    // -- immutable fields
    protected final String type;
    protected final FXSkinFactory skinFactory;
    // -- immutable fields

    public AbstractFXConnectionSkin(FXSkinFactory skinFactory, Parent parent, VFlow controller, String type) {
        setParent(parent);
        this.skinFactory = skinFactory;
        this.controller = controller;
        this.type = type;
    }

    @Override
    public final FXConnectionSkin init() {
        synchronized (this) {
            if (!initialized) {
                initSenderAndReceiver();
                initConnnectionPath();
                initStyle();
                initConnectionListener();
                makeDraggable();
                postInitialize();
                initialized = true;
            }
        }
        return this;
    }

    protected void postInitialize() {

    }

    protected abstract void initSenderAndReceiver();

    protected abstract void initStyle();

    protected void initConnnectionPath() {
        final Node senderNode = senderShape.getNode();

        DoubleBinding startXBinding = new DoubleBinding() {
            {
                super.bind(senderNode.layoutXProperty(),
                        senderNode.translateXProperty(),
                        senderShape.radiusProperty());
            }

            @Override
            protected double computeValue() {
                return senderNode.getLayoutX()
                        + senderNode.getTranslateX()
                        + senderShape.getRadius();
            }
        };

        DoubleBinding startYBinding = new DoubleBinding() {
            {
                super.bind(senderNode.layoutYProperty(),
                        senderNode.translateYProperty(),
                        senderShape.radiusProperty());
            }

            @Override
            protected double computeValue() {
                return senderNode.getLayoutY()
                        + senderNode.getTranslateY()
                        + senderShape.getRadius();
            }
        };

        DoubleBinding endXBinding = new DoubleBinding() {
            {
                super.bind(receiverConnectorUI.layoutXProperty(),
                        receiverConnectorUI.translateXProperty());
            }

            @Override
            protected double computeValue() {
                return receiverConnectorUI.getLayoutX() + receiverConnectorUI.getTranslateX();
            }
        };

        DoubleBinding endYBinding = new DoubleBinding() {
            {
                super.bind(receiverConnectorUI.layoutYProperty(),
                        receiverConnectorUI.translateYProperty());
            }

            @Override
            protected double computeValue() {
                return receiverConnectorUI.getLayoutY() + receiverConnectorUI.getTranslateY();
            }
        };

        DoubleBinding controlX1Binding = new DoubleBinding() {
            {
                super.bind(startXBinding, endXBinding);
            }

            @Override
            protected double computeValue() {
                return ( startXBinding.get() + endXBinding.get() ) / 2;
            }
        };

        DoubleBinding controlY1Binding = new DoubleBinding() {
            {
                super.bind(startYBinding);
            }

            @Override
            protected double computeValue() {
                return startYBinding.get();
            }
        };

        DoubleBinding controlX2Binding = new DoubleBinding() {
            {
                super.bind(startXBinding, endXBinding);
            }

            @Override
            protected double computeValue() {
                return ( startXBinding.get() + endXBinding.get() ) / 2;
            }
        };

        DoubleBinding controlY2Binding = new DoubleBinding() {
            {
                super.bind(endYBinding);
            }

            @Override
            protected double computeValue() {
                return endYBinding.get();
            }
        };

        MoveTo moveTo = new MoveTo();
        CubicCurveTo curveTo = new CubicCurveTo();
        connectionPath = new Path(moveTo, curveTo);
        Logger.getLogger(AbstractFXConnectionSkin.class.getName()).
                log(Level.INFO, "initConnnectionPath");

        moveTo.xProperty().bind(startXBinding);
        moveTo.yProperty().bind(startYBinding);

        curveTo.controlX1Property().bind(controlX1Binding);
        curveTo.controlY1Property().bind(controlY1Binding);
        curveTo.controlX2Property().bind(controlX2Binding);
        curveTo.controlY2Property().bind(controlY2Binding);
        curveTo.xProperty().bind(endXBinding);
        curveTo.yProperty().bind(endYBinding);

        /* Added */
        invisibleCurve = new CubicCurve();
        invisibleCurve.setStartX(moveTo.getX());
        invisibleCurve.setStartY(moveTo.getY());
        invisibleCurve.setControlX1(curveTo.getControlX1());
        invisibleCurve.setControlY1(curveTo.getControlY1());
        invisibleCurve.setControlX2(curveTo.getControlX2());
        invisibleCurve.setControlY2(curveTo.getControlY2());
        invisibleCurve.setEndX(curveTo.getX());
        invisibleCurve.setEndY(curveTo.getY());
        invisibleCurve.setStroke(Color.FORESTGREEN);
        invisibleCurve.setStrokeWidth(0);
        invisibleCurve.setStrokeLineCap(StrokeLineCap.ROUND);
        invisibleCurve.setFill(null);
        invisibleCurve.startXProperty().bind(moveTo.xProperty());
        invisibleCurve.startYProperty().bind(moveTo.yProperty());
        invisibleCurve.controlX1Property().bind(curveTo.controlX1Property());
        invisibleCurve.controlX2Property().bind(curveTo.controlX2Property());
        invisibleCurve.controlY1Property().bind(curveTo.controlY1Property());
        invisibleCurve.controlY2Property().bind(curveTo.controlY2Property());
        invisibleCurve.endXProperty().bind(curveTo.xProperty());
        invisibleCurve.endYProperty().bind(curveTo.yProperty());

        DoubleBinding xBinding = new DoubleBinding() {
            DoubleProperty endX = invisibleCurve.endXProperty();
            DoubleProperty startX = invisibleCurve.startXProperty();
            {
                this.bind(endX, startX);
            }
            @Override
            protected double computeValue() {
                double x = 0;
                PathTransition pt = new PathTransition(Duration.ONE, invisibleCurve, new Circle());
                pt.playFromStart(); // force initialization
                pt.stop();
                for (double frac = 0.0; frac <= 1.0; frac += 0.05) {
                    if (frac == 0.35) {
                        pt.interpolate(frac);
                        x = pt.getNode().getTranslateX();
                    }
                }
                return x-30;
            }
        };
        DoubleBinding yBinding = new DoubleBinding() {
            DoubleProperty endY = invisibleCurve.endYProperty();
            DoubleProperty startY = invisibleCurve.startYProperty();
            {
                this.bind(endY, startY);
            }
            @Override
            protected double computeValue() {
                double y = 0;
                PathTransition pt = new PathTransition(Duration.ONE, invisibleCurve, new Circle());
                pt.playFromStart(); // force initialization
                pt.stop();
                for (double frac = 0.0; frac <= 1.0; frac += 0.05) {
                    if (frac == 0.35) {
                        pt.interpolate(frac);
                        y = pt.getNode().getTranslateY();
                    }
                }
                return y;
            }
        };
        connectionText = new Text(nameProperty.getValue());
        connectionText.setX(200);
        connectionText.setY(200);
        connectionText.xProperty().bind(xBinding);
        connectionText.yProperty().bind(yBinding);
        connectionText.getStyleClass().add("connection-name");
    }

    protected void initConnectionListener() {
        connectionListener
                = new DefaultConnectionListener(
                        skinFactory, controller, receiverConnectorUI);
    }

    protected abstract void makeDraggable();

    protected Path getConnectionPath() {
        return connectionPath;
    }

    @Override
    public Connector getSender() {
        return senderProperty.get();
    }

    @Override
    public void setSender(Connector n) {
        senderProperty.set(n);
    }

    @Override
    public ObjectProperty<Connector> senderProperty() {
        return senderProperty;
    }

    @Override
    public Connector getReceiver() {
        return receiverProperty.get();
    }

    @Override
    public void setReceiver(Connector n) {
        receiverProperty.set(n);
    }

    @Override
    public ObjectProperty<Connector> receiverProperty() {
        return receiverProperty;
    }

    @Override
    public Path getNode() {
        return connectionPath;
    }

    @Override
    public Parent getContentNode() {
        return getParent();
    }

    @Override
    public void setModel(Connection model) {
        modelProperty.set(model);
    }

    @Override
    public Connection getModel() {
        return modelProperty.get();
    }

    @Override
    public ObjectProperty<Connection> modelProperty() {
        return modelProperty;
    }

    protected final void setParent(Parent parent) {
        parentProperty.set(parent);
    }

    protected Parent getParent() {
        return parentProperty.get();
    }

    protected ObjectProperty<Parent> parentProperty() {
        return parentProperty;
    }

    @Override
    public void add() {
        NodeUtil.addToParent(getParent(), connectionPath);
        NodeUtil.addToParent(getParent(), receiverConnectorUI);

        receiverConnectorUI.toFront();
        connectionPath.toBack();
    }

    @Override
    public void remove() {
        NodeUtil.removeFromParent(connectionPath);
        NodeUtil.removeFromParent(receiverConnectorUI);
        NodeUtil.removeFromParent(connectionText);
        NodeUtil.removeFromParent(invisibleCurve);
    }

    @Override
    public VFlow getController() {
        return controller;
    }

    @Override
    public void setController(VFlow controller) {
        this.controller = controller;
    }

    @Override
    public FXSkinFactory getSkinFactory() {
        return skinFactory;
    }

    @Override
    public void receiverToFront() {
        receiverConnectorUI.toFront();
    }

    @Override
    public Shape getReceiverUI() {
        return receiverConnectorUI;
    }

    @Override
    public ConnectorShape getSenderShape() {
        return senderShape;
    }

    @Override
    public ConnectorShape getReceiverShape() {
        return receiverShape;
    }

    public Text getConnectionText(){
        return this.connectionText;
    }
    public void setConnectionTextValue(String text){
        connectionText.setText(text);
    }
    public void setEventHandlerForConnectionText(EventHandler<MouseEvent> event){
        connectionText.addEventHandler(MouseEvent.MOUSE_CLICKED, event);
    }
}
