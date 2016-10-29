# TaskChain Changelog

## Version 3.0.1
* No changes. Version bump as I appear to of accidentally deployed the parent POM using 3.0.1-SNAPSHOT

## Version 3.0.0
* First release as a proper GitHub Project
* TaskChain API is now platform agnostic
* Chain Creation no longer uses a static TaskChain.newChain() API. Implementations must create a factory 
* Bugs with Shared Chains was fixed (unsafe concurrency, error handlers and done handlers may not of functioned correctly)
* TaskChain now uses its own Async Thread Pool
* During Shutdown of the game implementation, the shutdown process will block until the async queue is flushed. All chains will try to execute to completion on their current thread (will not switch back to main, since main will be blocked)
* .abortIfNull() now supports various "Actions". Game Implementations can provide common actions.
* A delay API based on real world time units added.
* Shared Chain helper methods that were based on Bukkit API removed.
* Artifacts will now be deployed to Aikar's nexus server and can be depended on without copying code to your project.

## Version 2.6
* Fixed bug where an exception in a task would not properly abort the chain, resulting in Shared Chains being forever stuck from executing.
* Changed the recent Error Handler to a BiConsumer to also pass the task that triggered the Exception (reason for minor bump)

## Version 2.5
* Fixed bug where Shared chains always executed next tick, breaking ability to execute synchronously
* While now unused internally, executeNext() will properly use 1 instead of 0 for next tick, as 0 can mean 'this tick' if scheduled inside of another bukkit scheduler task.
* Added Error Handler arguments to .execute(), so you can act on uncaught exceptions by your tasks.

## Version 2.5
* Added .returnChain() to the chain to also provide another way to access the chain in tasks, by passing the chain itself as an arg to the next task. chain.returnChain().async((chain) -> Object foo = chain.getTaskData("foo"); }).execute();

## Version 2.4
* Added Task.getCurrentChain() - get the currently executing chain in a task. Useful for generic tasks to get execution context.
