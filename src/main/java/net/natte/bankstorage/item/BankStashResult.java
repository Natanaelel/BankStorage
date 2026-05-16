package net.natte.bankstorage.item;

/**
 * Result of checking whether an item can be stashed into a bank.
 *
 * <p>Used by the client-side inventory interaction renderer to display colored
 * indicators on bank slots, and by the server-side right-click handlers to
 * determine if insertion should proceed.
 */
public enum BankStashResult {
    /**
     * The bank already contains this item type and has room to merge more.
     * Rendered as a green indicator.
     */
    MATCH_AND_SPACE,

    /**
     * The bank has an empty slot but does not yet contain this item type.
     * Rendered as a yellow indicator.
     */
    SPACE,

    /**
     * The bank is full or the item cannot be stored.
     * No indicator is rendered.
     */
    NO_SPACE
}
