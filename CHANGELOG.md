# TaskChain Changelog

## Version 3.7.1
* Bukkit: Execute sync tasks immediately if the plugin is disabled (scheduled during onDisable())

## Version 3.7.0
* Added Predicate support to .abortIf style methods

## Version 3.6.0
* Added .abortChain() API so you can insert an abort point for dynamic chain creation.

## Version 3.5.0
* Added .configure(TaskChain<>) API so you can dynamically insert into chain without breaking fluent interface.

## Version 3.4.3
* Added Java Sources and Java Docs
* Removed the shading of core into -bukkit and -sponge. It will be shaded on user level anyways (so that it can pick up the javadocs)

## Version 3.4.2
* Fixed: Concurrency Issue with Shared Chains and double execution

## Version 3.4.0
* New: Futures API - Developers may use the Java 8 "Promise" API CompleteableFuture as a way to control chain execution instead of the AsyncExecuting Callback method. Futures are much more powerful than the Callback style.
* New: Data Wrappers - You may now use TaskChain.multi(result1, result2, result3) to return multiple values from a task, to be accessed on the next task. This replaces the need to use the Task Data map temporarily between tasks to pass multiple values.
* Experimental: Support for Sponge (#1) has been added using Sponge API v6.0.0-SNAPSHOT. This is untested. 

## Version 3.3.4
* Rewrote the SharedTaskChain logic. All 3.x versions before this is assumed to have broken Shared Task Chain logic with rare concurrency issues.

## Version 3.3.3
* Fix POM files for dependencies to create the dependency reduced POM files to avoid double including in shade.

## Version 3.3.2
* Add abortIf() and .abortIfNot() API's. These behave like abortIfNull but allow you to check for a certain value.
* AbortAction interface was created to replace NullAction handlers, and TaskChainNullAction is now Deprecated

## Version 3.2.0
* Ability to set a default error handler on a Factory for all chains created by the factory to default to if they do not supply their own.

## Version 3.1.0
* TaskChain.getCurrentChain() now works in other user supplied methods such as Error Handlers, Done Handlers and Null Action Handlers. 
* User supplied methods such as those handlers should now properly catch all exceptions and avoid ever corrupting the chain processing (mainly for shared chains, as an error in your error handler on a shared chain could break the backing chain and prevent other chains from running)
* Add TaskChain.getCurrentActionIndex() API. Pretty much every thing you add to the chain will bump the action index, so you may use this value in your error/done handlers to know how far down the chain you were when it aborted, and make judgement there.
* Getters and Setters for Error and Done handlers were made public so you can change those if you really see a reason to mid execution.

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
