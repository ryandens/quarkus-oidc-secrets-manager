# Quarkus OIDC Extension and AWS Secrets Manager

This is a simple example of how to use the Quarkus OIDC extension with a `io.quarkus.credentials.CredentialsProvider`
implementation that leverages the `software.amazon.awssdk.services.secretsmanager.SecretsManagerClient` managed by
[Quarkus Amazon Services Secrets Manager](https://docs.quarkiverse.io/quarkus-amazon-services/2.12.x/amazon-secretsmanager.html).

This demonstrates the issue where the application is not able to initialize because the Quarkus OIDC extension
attempts to resolve the value of the client secret from the `CredentialsProvider` during the 
`io.quarkus.deployment.steps.OidcBuildStep` to validate the config. This is a problem because `RUNTIME_INIT` steps that 
require access to synthetic beans initialized during RUNTIME_INIT should consume the SyntheticBeansRuntimeInitBuildItem.

I think, in practice, the secret value should not be retrieved during the config validation and should instead be 
deferred until it is required (outside the scope of a build step) like was done in the Quarkus GitHub App Extension 
[here](https://github.com/quarkiverse/quarkus-github-app/pull/601).

This can be reproduced by running the `GreetingResourceTest` test in this project, or by running this Quarkus project 
in dev mode. A build scan of the test run is available: 
https://scans.gradle.com/s/5vyysmt4kfphi/tests/task/:test/details/org.acme.GreetingResourceTest/testHelloEndpoint()?top-execution=1


The application fails to start with the following exception:

```shell

```java
java.lang.RuntimeException: java.lang.RuntimeException: Failed to start quarkus
	at io.quarkus.test.junit.QuarkusTestExtension.throwBootFailureException(QuarkusTestExtension.java:642)
	at io.quarkus.test.junit.QuarkusTestExtension.interceptTestClassConstructor(QuarkusTestExtension.java:726)
	at java.base/java.util.Optional.orElseGet(Optional.java:364)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
	at java.base/java.util.ArrayList.forEach(ArrayList.java:1596)
Caused by: java.lang.RuntimeException: Failed to start quarkus
	at io.quarkus.runner.ApplicationImpl.doStart(Unknown Source)
	at io.quarkus.runtime.Application.start(Application.java:101)
	at java.base/java.lang.reflect.Method.invoke(Method.java:580)
	at io.quarkus.runner.bootstrap.StartupActionImpl.run(StartupActionImpl.java:285)
	at io.quarkus.test.junit.QuarkusTestExtension.doJavaStart(QuarkusTestExtension.java:251)
	at io.quarkus.test.junit.QuarkusTestExtension.ensureStarted(QuarkusTestExtension.java:609)
	at io.quarkus.test.junit.QuarkusTestExtension.beforeAll(QuarkusTestExtension.java:659)
	... 1 more
Caused by: org.gradle.internal.exceptions.DefaultMultiCauseException: Multiple exceptions caught:
	[Exception 0] jakarta.enterprise.inject.CreationException: Error creating synthetic bean [oRj83jIPijUzp7JkawwrGWJy-G8]: jakarta.enterprise.inject.CreationException: Synthetic bean instance for software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder not initialized yet: software_amazon_awssdk_services_secretsmanager_SecretsManagerClientBuilder_35edabeaf18440581dc996704efe9686730c9848
	- a synthetic bean initialized during RUNTIME_INIT must not be accessed during STATIC_INIT
	- RUNTIME_INIT build steps that require access to synthetic beans initialized during RUNTIME_INIT should consume the SyntheticBeansRuntimeInitBuildItem
	[Exception 1] io.quarkus.oidc.OIDCException
	at io.smallrye.mutiny.operators.uni.UniOnFailureFlatMap$UniOnFailureFlatMapProcessor.performInnerSubscription(UniOnFailureFlatMap.java:94)
	at io.smallrye.mutiny.operators.uni.UniOnFailureFlatMap$UniOnFailureFlatMapProcessor.dispatch(UniOnFailureFlatMap.java:83)
	at io.smallrye.mutiny.operators.uni.UniOnFailureFlatMap$UniOnFailureFlatMapProcessor.onFailure(UniOnFailureFlatMap.java:60)
	at io.smallrye.mutiny.operators.uni.UniOperatorProcessor.onFailure(UniOperatorProcessor.java:55)
	at io.smallrye.mutiny.operators.uni.UniOperatorProcessor.onFailure(UniOperatorProcessor.java:55)
	at io.smallrye.mutiny.operators.uni.UniOnItemOrFailureFlatMap$UniOnItemOrFailureFlatMapProcessor.performInnerSubscription(UniOnItemOrFailureFlatMap.java:91)
	at io.smallrye.mutiny.operators.uni.UniOnItemOrFailureFlatMap$UniOnItemOrFailureFlatMapProcessor.onItem(UniOnItemOrFailureFlatMap.java:54)
	at io.smallrye.mutiny.operators.uni.builders.UniCreateFromKnownItem$KnownItemSubscription.forward(UniCreateFromKnownItem.java:38)
	at io.smallrye.mutiny.operators.uni.builders.UniCreateFromKnownItem.subscribe(UniCreateFromKnownItem.java:23)
	at io.smallrye.mutiny.operators.AbstractUni.subscribe(AbstractUni.java:36)
	at io.smallrye.mutiny.operators.uni.UniOnItemOrFailureFlatMap.subscribe(UniOnItemOrFailureFlatMap.java:27)
	at io.smallrye.mutiny.operators.AbstractUni.subscribe(AbstractUni.java:36)
	at io.smallrye.mutiny.operators.uni.UniOnItemTransformToUni.subscribe(UniOnItemTransformToUni.java:25)
	at io.smallrye.mutiny.operators.AbstractUni.subscribe(AbstractUni.java:36)
	at io.smallrye.mutiny.operators.uni.UniOnItemTransform.subscribe(UniOnItemTransform.java:22)
	at io.smallrye.mutiny.operators.AbstractUni.subscribe(AbstractUni.java:36)
	at io.smallrye.mutiny.operators.uni.UniOnFailureFlatMap.subscribe(UniOnFailureFlatMap.java:31)
	at io.smallrye.mutiny.operators.AbstractUni.subscribe(AbstractUni.java:36)
	at io.smallrye.mutiny.operators.uni.UniBlockingAwait.await(UniBlockingAwait.java:60)
	at io.smallrye.mutiny.groups.UniAwait.atMost(UniAwait.java:65)
	at io.quarkus.oidc.runtime.OidcRecorder.createStaticTenantContext(OidcRecorder.java:166)
	at io.quarkus.oidc.runtime.OidcRecorder.setup(OidcRecorder.java:88)
	at io.quarkus.deployment.steps.OidcBuildStep$setup1008959783.deploy_0(Unknown Source)
	at io.quarkus.deployment.steps.OidcBuildStep$setup1008959783.deploy(Unknown Source)
	... 8 more
	Suppressed: io.quarkus.oidc.OIDCException
		at io.quarkus.oidc.runtime.OidcRecorder$5.apply(OidcRecorder.java:163)
		at io.quarkus.oidc.runtime.OidcRecorder$5.apply(OidcRecorder.java:145)
		at io.smallrye.context.impl.wrappers.SlowContextualFunction.apply(SlowContextualFunction.java:21)
		at io.smallrye.mutiny.groups.UniOnFailure.lambda$recoverWithItem$8(UniOnFailure.java:190)
		at io.smallrye.mutiny.operators.uni.UniOnFailureFlatMap$UniOnFailureFlatMapProcessor.performInnerSubscription(UniOnFailureFlatMap.java:92)
		... 31 more
Caused by: jakarta.enterprise.inject.CreationException: Error creating synthetic bean [oRj83jIPijUzp7JkawwrGWJy-G8]: jakarta.enterprise.inject.CreationException: Synthetic bean instance for software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder not initialized yet: software_amazon_awssdk_services_secretsmanager_SecretsManagerClientBuilder_35edabeaf18440581dc996704efe9686730c9848
	- a synthetic bean initialized during RUNTIME_INIT must not be accessed during STATIC_INIT
	- RUNTIME_INIT build steps that require access to synthetic beans initialized during RUNTIME_INIT should consume the SyntheticBeansRuntimeInitBuildItem
	at software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder_oRj83jIPijUzp7JkawwrGWJy-G8_Synthetic_Bean.doCreate(Unknown Source)
	at software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder_oRj83jIPijUzp7JkawwrGWJy-G8_Synthetic_Bean.create(Unknown Source)
	at software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder_oRj83jIPijUzp7JkawwrGWJy-G8_Synthetic_Bean.create(Unknown Source)
	at io.quarkus.arc.impl.AbstractSharedContext.createInstanceHandle(AbstractSharedContext.java:119)
	at io.quarkus.arc.impl.AbstractSharedContext$1.get(AbstractSharedContext.java:38)
	at io.quarkus.arc.impl.AbstractSharedContext$1.get(AbstractSharedContext.java:35)
	at io.quarkus.arc.generator.Default_jakarta_enterprise_context_ApplicationScoped_ContextInstances.c20(Unknown Source)
	at io.quarkus.arc.generator.Default_jakarta_enterprise_context_ApplicationScoped_ContextInstances.computeIfAbsent(Unknown Source)
	at io.quarkus.arc.impl.AbstractSharedContext.get(AbstractSharedContext.java:35)
	at io.quarkus.arc.impl.ClientProxies.getApplicationScopedDelegate(ClientProxies.java:21)
	at software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder_oRj83jIPijUzp7JkawwrGWJy-G8_Synthetic_ClientProxy.arc$delegate(Unknown Source)
	at software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder_oRj83jIPijUzp7JkawwrGWJy-G8_Synthetic_ClientProxy.build(Unknown Source)
	at io.quarkus.amazon.secretsmanager.runtime.SecretsManagerClientProducer.<init>(SecretsManagerClientProducer.java:21)
	at io.quarkus.amazon.secretsmanager.runtime.SecretsManagerClientProducer_Bean.doCreate(Unknown Source)
	at io.quarkus.amazon.secretsmanager.runtime.SecretsManagerClientProducer_Bean.create(Unknown Source)
	at io.quarkus.amazon.secretsmanager.runtime.SecretsManagerClientProducer_Bean.create(Unknown Source)
	at io.quarkus.arc.impl.AbstractSharedContext.createInstanceHandle(AbstractSharedContext.java:119)
	at io.quarkus.arc.impl.AbstractSharedContext$1.get(AbstractSharedContext.java:38)
	at io.quarkus.arc.impl.AbstractSharedContext$1.get(AbstractSharedContext.java:35)
	at io.quarkus.arc.generator.Default_jakarta_enterprise_context_ApplicationScoped_ContextInstances.c11(Unknown Source)
	at io.quarkus.arc.generator.Default_jakarta_enterprise_context_ApplicationScoped_ContextInstances.computeIfAbsent(Unknown Source)
	at io.quarkus.arc.impl.AbstractSharedContext.get(AbstractSharedContext.java:35)
	at io.quarkus.arc.impl.ClientProxies.getApplicationScopedDelegate(ClientProxies.java:21)
	at io.quarkus.amazon.secretsmanager.runtime.SecretsManagerClientProducer_ClientProxy.arc$delegate(Unknown Source)
	at io.quarkus.amazon.secretsmanager.runtime.SecretsManagerClientProducer_ClientProxy.arc_contextualInstance(Unknown Source)
	at io.quarkus.amazon.secretsmanager.runtime.SecretsManagerClientProducer_ProducerMethod_client_o2u827DJgDuZ1zCfbjV6TqIsxgk_Bean.doCreate(Unknown Source)
	at io.quarkus.amazon.secretsmanager.runtime.SecretsManagerClientProducer_ProducerMethod_client_o2u827DJgDuZ1zCfbjV6TqIsxgk_Bean.create(Unknown Source)
	at io.quarkus.amazon.secretsmanager.runtime.SecretsManagerClientProducer_ProducerMethod_client_o2u827DJgDuZ1zCfbjV6TqIsxgk_Bean.create(Unknown Source)
	at io.quarkus.arc.impl.AbstractSharedContext.createInstanceHandle(AbstractSharedContext.java:119)
	at io.quarkus.arc.impl.AbstractSharedContext$1.get(AbstractSharedContext.java:38)
	at io.quarkus.arc.impl.AbstractSharedContext$1.get(AbstractSharedContext.java:35)
	at io.quarkus.arc.generator.Default_jakarta_enterprise_context_ApplicationScoped_ContextInstances.c13(Unknown Source)
	at io.quarkus.arc.generator.Default_jakarta_enterprise_context_ApplicationScoped_ContextInstances.computeIfAbsent(Unknown Source)
	at io.quarkus.arc.impl.AbstractSharedContext.get(AbstractSharedContext.java:35)
	at io.quarkus.arc.impl.ClientProxies.getApplicationScopedDelegate(ClientProxies.java:21)
	at software.amazon.awssdk.services.secretsmanager.SecretsManagerClientProducer_ProducerMethod_client_o2u827DJgDuZ1zCfbjV6TqIsxgk_ClientProxy.arc$delegate(Unknown Source)
	at software.amazon.awssdk.services.secretsmanager.SecretsManagerClientProducer_ProducerMethod_client_o2u827DJgDuZ1zCfbjV6TqIsxgk_ClientProxy.getSecretValue(Unknown Source)
	at org.acme.SecretsManagerCredentialsProvider.getCredentials(SecretsManagerCredentialsProvider.java:45)
	at org.acme.SecretsManagerCredentialsProvider_ClientProxy.getCredentials(Unknown Source)
	at io.quarkus.oidc.common.runtime.OidcCommonUtils$1.get(OidcCommonUtils.java:317)
	at io.quarkus.oidc.common.runtime.OidcCommonUtils$1.get(OidcCommonUtils.java:309)
	at java.base@21.0.3/java.util.Optional.orElseGet(Optional.java:364)
	at io.quarkus.oidc.common.runtime.OidcCommonUtils.clientSecret(OidcCommonUtils.java:297)
	at io.quarkus.oidc.common.runtime.OidcCommonUtils.initClientSecretBasicAuth(OidcCommonUtils.java:421)
	at io.quarkus.oidc.runtime.OidcProviderClient.<init>(OidcProviderClient.java:66)
	at io.quarkus.oidc.runtime.OidcRecorder$11.apply(OidcRecorder.java:556)
	at io.quarkus.oidc.runtime.OidcRecorder$11.apply(OidcRecorder.java:523)
	at io.smallrye.context.impl.wrappers.SlowContextualBiFunction.apply(SlowContextualBiFunction.java:21)
	at io.smallrye.mutiny.operators.uni.UniOnItemOrFailureFlatMap$UniOnItemOrFailureFlatMapProcessor.performInnerSubscription(UniOnItemOrFailureFlatMap.java:86)
	... 26 more
Caused by: jakarta.enterprise.inject.CreationException: Synthetic bean instance for software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder not initialized yet: software_amazon_awssdk_services_secretsmanager_SecretsManagerClientBuilder_35edabeaf18440581dc996704efe9686730c9848
	- a synthetic bean initialized during RUNTIME_INIT must not be accessed during STATIC_INIT
	- RUNTIME_INIT build steps that require access to synthetic beans initialized during RUNTIME_INIT should consume the SyntheticBeansRuntimeInitBuildItem
	at software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder_oRj83jIPijUzp7JkawwrGWJy-G8_Synthetic_Bean.createSynthetic(Unknown Source)
	... 75 more
```
