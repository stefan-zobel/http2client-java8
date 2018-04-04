package lib;

import java.util.OptionalInt;
import java.util.function.IntConsumer;

public final class Optionals {

    /**
     * If a value is present, performs the given action with the value,
     * otherwise performs the given empty-based action.
     *
     * @param action
     *            the action to be performed, if a value is present
     * @param emptyAction
     *            the empty-based action to be performed, if no value is present
     * @throws NullPointerException
     *             if a value is present and the given action is {@code null},
     *             or no value is present and the given empty-based action is
     *             {@code null}.
     * @since 9
     */
    public static void ifPresentOrElse(OptionalInt opt, IntConsumer action, Runnable emptyAction) {
        if (opt.isPresent()) {
            action.accept(opt.getAsInt());
        } else {
            emptyAction.run();
        }
    }

    private Optionals() {
        throw new AssertionError();
    }
}
