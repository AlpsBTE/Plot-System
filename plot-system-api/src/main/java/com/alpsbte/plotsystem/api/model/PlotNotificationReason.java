package com.alpsbte.plotsystem.api.model;

/**
 * Why an integration is about to notify about a plot.
 *
 * <p>These reasons describe the notification intent, not a Plot System lifecycle
 * outcome. Integrations that send notifications (Discord, webhooks, etc.) should
 * fire {@link com.alpsbte.plotsystem.api.event.PlotNotificationIntentEvent} with
 * the matching reason immediately before delivery. Plot System does not send
 * these notifications itself.</p>
 */
public enum PlotNotificationReason {
    /** Plot was created or claimed. */
    CREATED,

    /** Plot was submitted for review. */
    SUBMITTED,

    /** Review feedback was updated without a new approve/reject outcome. */
    REVIEWED,

    /** Plot was approved. */
    APPROVED,

    /** Plot was rejected. */
    REJECTED,

    /** A prior review was undone. */
    UNDO_REVIEW,

    /** A prior submission was undone. */
    UNDO_SUBMIT,

    /** Plot was highlighted or showcased by an integration. */
    SHOWCASED,

    /** Plot was abandoned. */
    ABANDONED,

    /** Plot was archived. */
    ARCHIVED,

    /**
     * Integration-owned inactivity reminder. Plot System does not schedule or
     * emit these; the integration decides timing and may cancel via the intent
     * event like any other reason.
     */
    INACTIVITY
}
