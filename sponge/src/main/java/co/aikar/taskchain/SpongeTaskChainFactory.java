package co.aikar.taskchain;

public class SpongeTaskChainFactory extends TaskChainFactory {

    private SpongeTaskChainFactory(GameInterface impl) {
        super(impl);
    }
    public static TaskChainFactory create(Object plugin) {
        // TODO: Ensure it's a plugin?
        return new SpongeTaskChainFactory(new SpongeGameInterface(plugin));
    }

    private static class SpongeGameInterface implements GameInterface {
        private final AsyncQueue asyncQueue;

        public SpongeGameInterface(Object plugin) {
            this.asyncQueue = new TaskChainAsyncQueue();
        }

        @Override
        public boolean isMainThread() {
            return false;
        }

        @Override
        public AsyncQueue getAsyncQueue() {
            return asyncQueue;
        }

        @Override
        public void postToMain(Runnable run) {

        }

        @Override
        public void postAsync(Runnable run) {
            asyncQueue.postAsync(run);
        }

        @Override
        public void scheduleTask(int gameUnits, Runnable run) {

        }

        @Override
        public void registerShutdownHandler(TaskChainFactory factory) {

        }
    }
}
