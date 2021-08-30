package lv.venta.repositories;

import lv.venta.models.GroupParameter;
import lv.venta.models.Parameter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParameterRepo extends JpaRepository<Parameter, Long> {
    Parameter findByParamGroupAndValue(GroupParameter groupParameter, String value);
}
