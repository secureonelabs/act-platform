package no.mnemonic.act.platform.seb.producer.v1.resolvers;

import no.mnemonic.act.platform.dao.cassandra.FactManager;
import no.mnemonic.act.platform.dao.cassandra.entity.FactTypeEntity;
import no.mnemonic.act.platform.seb.model.v1.FactTypeInfoSEB;

import javax.inject.Inject;
import java.util.UUID;
import java.util.function.Function;

public class FactTypeInfoResolver implements Function<UUID, FactTypeInfoSEB> {

  private final FactManager factManager;

  @Inject
  public FactTypeInfoResolver(FactManager factManager) {
    this.factManager = factManager;
  }

  @Override
  public FactTypeInfoSEB apply(UUID id) {
    if (id == null) return null;

    FactTypeEntity type = factManager.getFactType(id);
    if (type == null) return null;

    return FactTypeInfoSEB.builder()
            .setId(type.getId())
            .setName(type.getName())
            .build();
  }
}
