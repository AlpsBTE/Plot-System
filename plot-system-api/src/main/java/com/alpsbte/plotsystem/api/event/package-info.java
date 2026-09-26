/**
 * Stable lifecycle events published by Plot System for external integrations.
 *
 * <p>Integrations should register a Bukkit listener and translate these events
 * into their own transport or application API. Plot System does not depend on
 * any integration implementation.</p>
 *
 * <pre>{@code
 * public final class MyIntegrationListener implements Listener {
 *     @EventHandler
 *     public void onPlotSubmitted(PlotSubmittedEvent event) {
 *         // Translate event.getPlot() to the integration's model.
 *     }
 * }
 * }</pre>
 */
package com.alpsbte.plotsystem.api.event;
