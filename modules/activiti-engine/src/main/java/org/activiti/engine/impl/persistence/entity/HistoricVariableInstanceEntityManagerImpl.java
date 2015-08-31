/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.persistence.entity;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.impl.HistoricVariableInstanceQueryImpl;
import org.activiti.engine.impl.Page;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.persistence.CachedPersistentObjectMatcher;

/**
 * @author Christian Lipphardt (camunda)
 * @author Joram Barrez
 */
public class HistoricVariableInstanceEntityManagerImpl extends AbstractEntityManager<HistoricVariableInstanceEntity> implements HistoricVariableInstanceEntityManager {
  
  @Override
  public Class<HistoricVariableInstanceEntity> getManagedPersistentObject() {
    return HistoricVariableInstanceEntity.class;
  }
  
  @Override
  public HistoricVariableInstanceEntity copyAndInsert(VariableInstanceEntity variableInstance) {
    HistoricVariableInstanceEntity historicVariableInstance = new HistoricVariableInstanceEntity();
    historicVariableInstance.setId(variableInstance.getId());
    historicVariableInstance.setProcessInstanceId(variableInstance.getProcessInstanceId());
    historicVariableInstance.setExecutionId(variableInstance.getExecutionId());
    historicVariableInstance.setTaskId(variableInstance.getTaskId());
    historicVariableInstance.setRevision(variableInstance.getRevision());
    historicVariableInstance.setName(variableInstance.getName());
    historicVariableInstance.setVariableType(variableInstance.getType());

    copyVariableValue(historicVariableInstance, variableInstance);

    Date time = Context.getProcessEngineConfiguration().getClock().getCurrentTime();
    historicVariableInstance.setCreateTime(time);
    historicVariableInstance.setLastUpdatedTime(time);

    insert(historicVariableInstance);

    return historicVariableInstance;
  }
  
  @Override
  public void copyVariableValue(HistoricVariableInstanceEntity historicVariableInstance, VariableInstanceEntity variableInstance) {
    historicVariableInstance.textValue = variableInstance.getTextValue();
    historicVariableInstance.textValue2 = variableInstance.getTextValue2();
    historicVariableInstance.doubleValue = variableInstance.getDoubleValue();
    historicVariableInstance.longValue = variableInstance.getLongValue();

    historicVariableInstance.variableType = variableInstance.getType();
    if (variableInstance.getByteArrayRef() != null) {
      historicVariableInstance.setBytes(variableInstance.getBytes());
    }

    historicVariableInstance.lastUpdatedTime = Context.getProcessEngineConfiguration().getClock().getCurrentTime();
  }
  
  @Override
  public void delete(HistoricVariableInstanceEntity entity, boolean fireDeleteEvent) {
    super.delete(entity, fireDeleteEvent);
    
    if (entity.getByteArrayRef() != null) {
      entity.getByteArrayRef().delete();
    }
  }

  @Override
  public void deleteHistoricVariableInstanceByProcessInstanceId(final String historicProcessInstanceId) {
    if (getHistoryManager().isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      
      List<HistoricVariableInstanceEntity> historicProcessVariables = getList("selectHistoricVariableInstanceByProcessInstanceId", 
          historicProcessInstanceId, new CachedPersistentObjectMatcher<HistoricVariableInstanceEntity>() {
        
        @Override
        public boolean isRetained(HistoricVariableInstanceEntity historicVariableInstanceEntity) {
          return historicVariableInstanceEntity.getProcessInstanceId() != null && historicVariableInstanceEntity.getProcessInstanceId().equals(historicProcessInstanceId);
        }
        
      }, true);

      for (HistoricVariableInstanceEntity historicProcessVariable : historicProcessVariables) {
        delete(historicProcessVariable);
      }

    }
  }

  @Override
  public long findHistoricVariableInstanceCountByQueryCriteria(HistoricVariableInstanceQueryImpl historicProcessVariableQuery) {
    return (Long) getDbSqlSession().selectOne("selectHistoricVariableInstanceCountByQueryCriteria", historicProcessVariableQuery);
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<HistoricVariableInstance> findHistoricVariableInstancesByQueryCriteria(HistoricVariableInstanceQueryImpl historicProcessVariableQuery, Page page) {
    return getDbSqlSession().selectList("selectHistoricVariableInstanceByQueryCriteria", historicProcessVariableQuery, page);
  }

  @Override
  public HistoricVariableInstanceEntity findHistoricVariableInstanceByVariableInstanceId(String variableInstanceId) {
    return (HistoricVariableInstanceEntity) getDbSqlSession().selectOne("selectHistoricVariableInstanceByVariableInstanceId", variableInstanceId);
  }

  @Override
  public void deleteHistoricVariableInstancesByTaskId(String taskId) {
    if (getHistoryManager().isHistoryLevelAtLeast(HistoryLevel.ACTIVITY)) {
      List<HistoricVariableInstance> historicProcessVariables = new HistoricVariableInstanceQueryImpl().taskId(taskId).list();

      for (HistoricVariableInstance historicProcessVariable : historicProcessVariables) {
        delete((HistoricVariableInstanceEntity) historicProcessVariable);
      }
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public List<HistoricVariableInstance> findHistoricVariableInstancesByNativeQuery(Map<String, Object> parameterMap, int firstResult, int maxResults) {
    return getDbSqlSession().selectListWithRawParameter("selectHistoricVariableInstanceByNativeQuery", parameterMap, firstResult, maxResults);
  }

  @Override
  public long findHistoricVariableInstanceCountByNativeQuery(Map<String, Object> parameterMap) {
    return (Long) getDbSqlSession().selectOne("selectHistoricVariableInstanceCountByNativeQuery", parameterMap);
  }
}
