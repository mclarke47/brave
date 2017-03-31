package com.github.kristofa.brave.jaxrs2;

import com.github.kristofa.brave.Brave;
import com.github.kristofa.brave.http.DefaultSpanNameProvider;
import com.github.kristofa.brave.http.SpanNameProvider;

import javax.inject.Inject;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseFilter;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.Provider;

import static com.github.kristofa.brave.internal.Util.checkNotNull;

@Provider
public final class BraveTracingFeature implements Feature {

  /** Creates a tracing feature with defaults. Use {@link #builder(Brave)} to customize. */
  public static BraveTracingFeature create(Brave brave) {
    return new Builder(brave).build();
  }

  public static Builder builder(Brave brave) {
    return new Builder(brave);
  }

  public static final class Builder {
    final Brave brave;
    SpanNameProvider spanNameProvider = new DefaultSpanNameProvider();

    Builder(Brave brave) { // intentionally hidden
      this.brave = checkNotNull(brave, "brave");
    }

    public Builder spanNameProvider(SpanNameProvider spanNameProvider) {
      this.spanNameProvider = checkNotNull(spanNameProvider, "spanNameProvider");
      return this;
    }

    public BraveTracingFeature build() {
      return new BraveTracingFeature(this);
    }
  }

  final Brave brave;
  final SpanNameProvider spanNameProvider;

  BraveTracingFeature(Builder b) { // intentionally hidden
    this.brave = b.brave;
    this.spanNameProvider = b.spanNameProvider;
  }

  @Inject // internal dependency-injection constructor
  BraveTracingFeature(Brave brave, SpanNameProvider spanNameProvider) {
    this(builder(brave).spanNameProvider(spanNameProvider));
  }

  @Override
  public boolean configure(FeatureContext context) {
    context.register(getClientRequestFeature(brave, spanNameProvider));
    context.register(getClientResponseFilter(brave));
    context.register(getContainerRequestFilter(brave, spanNameProvider));
    context.register(getContainerResponseFilter(brave));
    return true;
  }

  protected ClientRequestFilter getClientRequestFeature(Brave brave, SpanNameProvider spanNameProvider) {
    return BraveClientRequestFilter.builder(brave).spanNameProvider(spanNameProvider).build();
  }

  protected ClientResponseFilter getClientResponseFilter(Brave brave){
    return BraveClientResponseFilter.create(brave);
  }
  protected ContainerRequestFilter getContainerRequestFilter(Brave brave, SpanNameProvider spanNameProvider){
    return BraveContainerRequestFilter.builder(brave).spanNameProvider(spanNameProvider).build();
  }

  protected ContainerResponseFilter getContainerResponseFilter(Brave brave){
    return BraveContainerResponseFilter.create(brave);
  }

}
