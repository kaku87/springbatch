package com.example.springbatch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.batch.core.configuration.DuplicateJobException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.support.ReferenceJobFactory;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 単一Taskletで構成されたJobを簡単に登録するためのテンプレート。
 * <p>
 * サブクラスは {@link #jobName()} と {@link #taskletClass()} を実装するだけで、
 * Job / Step bean が自動的に登録される。
 */
public abstract class AbstractSingleTaskletJobConfiguration implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(AbstractSingleTaskletJobConfiguration.class);

    @Autowired
    private GenericApplicationContext applicationContext;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private JobRegistry jobRegistry;

    @Override
    public void afterPropertiesSet() {
        registerJobDefinition();
    }

    protected abstract String jobName();

    protected String stepName() {
        return jobName() + "Step";
    }

    protected abstract Class<? extends Tasklet> taskletClass();

    protected Tasklet resolveTasklet() {
        Class<? extends Tasklet> targetClass = taskletClass();
        ObjectProvider<Tasklet> provider = applicationContext.getBeanProvider(Tasklet.class);
        return provider.stream()
                .filter(tasklet -> targetClass.isAssignableFrom(AopProxyUtils.ultimateTargetClass(tasklet)))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("指定されたTaskletが見つかりません: " + targetClass.getName()));
    }

    private void registerJobDefinition() {
        String jobName = jobName();
        String stepName = stepName();

        if (!applicationContext.containsBean(stepName)) {
            applicationContext.registerBean(stepName, Step.class,
                    () -> buildTaskletStep(stepName, resolveTasklet(), jobRepository, transactionManager));
        }

        if (!applicationContext.containsBean(jobName)) {
            applicationContext.registerBean(jobName, Job.class,
                    () -> buildSimpleJob(jobName,
                            applicationContext.getBean(stepName, Step.class),
                            jobRepository));
        }

        registerToJobRegistry(jobName);
    }

    private void registerToJobRegistry(String jobName) {
        try {
            Job job = applicationContext.getBean(jobName, Job.class);
            jobRegistry.register(new ReferenceJobFactory(job));
        } catch (DuplicateJobException e) {
            logger.debug("Job [{}] is already registered. Skipping duplicate registration.", jobName);
        }
    }

    protected Step buildTaskletStep(String stepName,
                                    Tasklet tasklet,
                                    JobRepository jobRepository,
                                    PlatformTransactionManager transactionManager) {
        return new StepBuilder(stepName, jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    protected Job buildSimpleJob(String jobName,
                                 Step firstStep,
                                 JobRepository jobRepository) {
        return new JobBuilder(jobName, jobRepository)
                .start(firstStep)
                .build();
    }
}
