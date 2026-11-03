/**
 * Stable lifecycle events published by Plot System for external integrations.
 *
 * <p>Integrations should register a Bukkit listener and translate these events
 * into their own transport or application API. Plot System does not depend on
 * any integration implementation. Tutorial plots are out of scope for these
 * events (temporary world cleanup does not emit archive/abandon).</p>
 *
 * <p>For integration-owned inactivity reminders, use
 * {@link com.alpsbte.plotsystem.api.provider.PlotSnapshotProvider} to reconcile
 * persisted schedules on startup and before delivery. Reschedule on
 * {@link com.alpsbte.plotsystem.api.event.PlotActivityChangedEvent} and
 * {@link com.alpsbte.plotsystem.api.event.PlotStatusChangedEvent}, and consume
 * review and abandonment events for the completion of those operations.
 * Plot System does not emit scheduled inactivity warnings; the former
 * {@code PlotInactivityWarningEvent} has been removed.</p>
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
