package com.oneops.inductor;

import com.oneops.cms.execution.ComponentWoExecutor;
import com.oneops.cms.execution.Response;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ClassMatchingWoExecutor implements ComponentWoExecutor {

  private static final Logger logger = Logger.getLogger(ClassMatchingWoExecutor.class);

  Map<String, ComponentWoExecutor> executorMap = new HashMap<>();

  @Autowired(required = false)
  ComponentWoExecutor[] executors;

  @Override
  public List<String> getComponentClasses() {
    return Arrays.asList("*");
  }

  @PostConstruct
  public void init() {
    if (executors != null) {
      executorMap = Stream.of(executors).
          flatMap(e -> e.getComponentClasses().stream().map(c -> new AbstractMap.SimpleEntry<>(c, e))).
          collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }
    logger.info("Custom executors " + executorMap);
  }

  @Override
  public Response execute(CmsWorkOrderSimple wo) {
    String className = wo.getRfcCi().getCiClassName();
    if (executorMap.containsKey(className)) {
      ComponentWoExecutor executor = executorMap.get(className);
      logger.info("executing by " + executor.getClass().getName());
      return executor.execute(wo);
    }
    return Response.getNotMatchingResponse();
  }

  @Override
  public Response executeAndVerify(CmsWorkOrderSimple wo) {
    String className = wo.getRfcCi().getCiClassName();
    if (executorMap.containsKey(className)) {
      ComponentWoExecutor executor = executorMap.get(className);
      logger.info("executing with verify by " + executor.getClass().getName());
      return executor.executeAndVerify(wo);
    }
    return Response.getNotMatchingResponse();
  }

  @Override
  public Response verify(CmsWorkOrderSimple wo, Response response) {
    return response;
  }

}
