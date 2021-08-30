package lv.venta.repositories;

import lv.venta.models.GroupParameter;
import lv.venta.models.Item;
import lv.venta.models.JoinItemParams;
import lv.venta.models.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface JoinItemParamsRepo extends JpaRepository<JoinItemParams, Long> {

    JoinItemParams findByJoinItemAndJoinGroupParam(Item joinItem, GroupParameter joinGroupParam);
    ArrayList<JoinItemParams> findByJoinGroupParamAndJoinParam(GroupParameter groupParameter, Parameter parameter);
}
