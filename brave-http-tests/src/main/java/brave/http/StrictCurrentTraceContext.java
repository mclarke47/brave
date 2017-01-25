package brave.http;

import brave.propagation.CurrentTraceContext;
import brave.propagation.TraceContext;

import static org.assertj.core.api.Assertions.assertThat;

class StrictCurrentTraceContext implements CurrentTraceContext {
  // intentionally not inheritable to ensure instrumentation propagation doesn't accidentally work
  final ThreadLocal<TraceContext> local = new ThreadLocal<>();

  @Override public TraceContext get() {
    return local.get();
  }

  @Override public Scope newScope(TraceContext currentSpan) {
    long threadId = Thread.currentThread().getId();
    final TraceContext previous = local.get();
    local.set(currentSpan);
    return () -> {
      assertThat(Thread.currentThread().getId())
          .withFailMessage("scope should be started and stopped in the same thread")
          .isEqualTo(threadId);
      local.set(previous);
    };
  }
}
