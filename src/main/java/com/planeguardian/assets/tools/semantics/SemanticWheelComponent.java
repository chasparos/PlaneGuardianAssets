package com.planeguardian.assets.tools.semantics;

import com.planeguardian.assets.generation.semantics.SemanticWheelDefinition;
import com.planeguardian.assets.generation.semantics.SemanticWheelValue;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/** Custom-painted draggable semantic coordinate constrained to a circle. */
public final class SemanticWheelComponent extends JComponent {
    private final SemanticWheelDefinition definition;
    private final List<Consumer<SemanticWheelValue>> listeners = new CopyOnWriteArrayList<>();
    private SemanticWheelValue value = SemanticWheelValue.centered(0);

    public SemanticWheelComponent(SemanticWheelDefinition definition) {
        this.definition = java.util.Objects.requireNonNull(definition);
        setPreferredSize(new Dimension(250, 250)); setMinimumSize(new Dimension(180, 180));
        MouseAdapter drag = new MouseAdapter() {
            @Override public void mousePressed(MouseEvent event) { movePoint(event.getX(), event.getY()); }
            @Override public void mouseDragged(MouseEvent event) { movePoint(event.getX(), event.getY()); }
        };
        addMouseListener(drag); addMouseMotionListener(drag);
    }
    public SemanticWheelValue value() { return value; }
    public void setValue(SemanticWheelValue value) { this.value = java.util.Objects.requireNonNull(value); repaint(); listeners.forEach(listener -> listener.accept(value)); }
    public void setSalience(double salience) { setValue(new SemanticWheelValue(value.x(), value.y(), salience, value.relationshipMode(), value.focus(), value.secondaryPoles())); }
    public void addValueListener(Consumer<SemanticWheelValue> listener) { listeners.add(java.util.Objects.requireNonNull(listener)); }

    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int radius = Math.max(20, Math.min(getWidth(), getHeight()) / 2 - 36); int cx = getWidth() / 2; int cy = getHeight() / 2;
        g.setColor(new Color(238, 232, 210)); g.fillOval(cx-radius, cy-radius, radius*2, radius*2);
        g.setColor(new Color(72, 59, 45)); g.setStroke(new BasicStroke(2)); g.drawOval(cx-radius, cy-radius, radius*2, radius*2);
        Font baseFont = getFont();
        if (baseFont == null) baseFont = UIManager.getFont("Label.font");
        if (baseFont == null) baseFont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
        g.setFont(baseFont.deriveFont(10f));
        for (SemanticWheelDefinition.Sector sector : definition.sectors()) {
            double dx = StrictMath.cos(sector.angleRadians()), dy = StrictMath.sin(sector.angleRadians());
            g.setColor(new Color(125, 108, 82)); g.drawLine(cx, cy, cx+(int)(dx*radius), cy-(int)(dy*radius));
            FontMetrics fm = g.getFontMetrics(); int tx = cx+(int)(dx*(radius+17))-fm.stringWidth(sector.label())/2;
            int ty = cy-(int)(dy*(radius+17))+fm.getAscent()/2; g.setColor(new Color(50, 42, 34)); g.drawString(sector.label(), tx, ty);
        }
        int px = cx + (int)(value.x()*radius), py = cy - (int)(value.y()*radius);
        g.setColor(new Color(186, 65, 48)); g.fillOval(px-7, py-7, 14, 14); g.setColor(Color.WHITE); g.drawOval(px-7, py-7, 14, 14);
        g.dispose();
    }
    void movePoint(int mouseX, int mouseY) {
        int radius = Math.max(20, Math.min(getWidth(), getHeight()) / 2 - 36); double x = (mouseX-getWidth()/2.0)/radius; double y = -(mouseY-getHeight()/2.0)/radius;
        double length = StrictMath.hypot(x, y); if (length > 1) { x /= length; y /= length; }
        setValue(new SemanticWheelValue(x, y, value.salience(), Optional.empty(), Optional.empty(), List.of()));
    }
}
