/**
 * Stable lifecycle and notification-intent events published for external
 * integrations.
 *
 * <p>Integrations should register a Bukkit listener and translate lifecycle
 * events into their own transport or application API. Plot System does not
 * depend on any integration implementation. Tutorial plots are out of scope
 * for these events (temporary world cleanup does not emit archive/abandon).</p>
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
 * <h2>Notification veto</h2>
 *
 * <p>Lifecycle events are observe-only and not {@link org.bukkit.event.Cancellable}.
 * To let third parties veto a notification without cancelling plot work,
 * notifiers must fire
 * {@link com.alpsbte.plotsystem.api.event.PlotNotificationIntentEvent} on the
 * server thread immediately before delivery and skip sending when cancelled.
 * Plot System does not fire that event itself — delivery ownership stays with
 * the integration (including inactivity reminders and showcase).</p>
 *
 * <pre>{@code
 * public final class MyIntegrationListener implements Listener {
 *     @EventHandler
 *     public void onPlotSubmitted(PlotSubmittedEvent event) {
 *         // Translate event.getPlot() to the integration's model.
 *     }
 *
 *     @EventHandler
 *     public void onNotificationIntent(PlotNotificationIntentEvent event) {
 *         if (event.getReason() == PlotNotificationReason.INACTIVITY) {
 *             event.setCancelled(true);
 *         }
 *     }
 * }
 * }</pre>
 */
package com.alpsbte.plotsystem.api.event;
