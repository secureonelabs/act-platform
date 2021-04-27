package no.mnemonic.act.platform.service.ti.tinkerpop;

import no.mnemonic.act.platform.dao.api.record.FactRecord;
import no.mnemonic.act.platform.dao.api.record.ObjectRecord;
import no.mnemonic.act.platform.service.ti.tinkerpop.utils.ObjectFactTypeResolver.FactTypeStruct;
import no.mnemonic.act.platform.service.ti.tinkerpop.utils.ObjectFactTypeResolver.ObjectTypeStruct;
import no.mnemonic.act.platform.service.ti.tinkerpop.utils.PropertyEntry;
import no.mnemonic.commons.utilities.collections.MapUtils;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Property;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.StringFactory;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import static no.mnemonic.commons.utilities.collections.ListUtils.list;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.*;

public class FactEdgeTest extends AbstractGraphTest {

  @Test
  public void testCreateEdgeWithoutGraph() {
    assertThrows(RuntimeException.class, () -> FactEdge.builder()
            .setFactRecord(new FactRecord())
            .setFactType(FactTypeStruct.builder().build())
            .setInVertex(mock(Vertex.class))
            .setOutVertex(mock(Vertex.class))
            .build());
  }

  @Test
  public void testCreateEdgeWithoutFact() {
    assertThrows(RuntimeException.class, () -> FactEdge.builder()
            .setGraph(getActGraph())
            .setFactType(FactTypeStruct.builder().build())
            .setInVertex(mock(Vertex.class))
            .setOutVertex(mock(Vertex.class))
            .build());
  }

  @Test
  public void testCreateEdgeWithoutFactType() {
    assertThrows(RuntimeException.class, () -> FactEdge.builder()
            .setGraph(getActGraph())
            .setFactRecord(new FactRecord())
            .setInVertex(mock(Vertex.class))
            .setOutVertex(mock(Vertex.class))
            .build());
  }

  @Test
  public void testCreateEdgeWithoutInVertex() {
    assertThrows(RuntimeException.class, () -> FactEdge.builder()
            .setGraph(getActGraph())
            .setFactRecord(new FactRecord())
            .setFactType(FactTypeStruct.builder().build())
            .setOutVertex(mock(Vertex.class))
            .build());
  }

  @Test
  public void testCreateEdgeWithoutOutVertex() {
    assertThrows(RuntimeException.class, () -> FactEdge.builder()
            .setGraph(getActGraph())
            .setFactRecord(new FactRecord())
            .setFactType(FactTypeStruct.builder().build())
            .setInVertex(mock(Vertex.class))
            .build());
  }

  @Test
  public void testCreateEdge() {
    UUID factId = UUID.randomUUID();
    FactEdge edge = FactEdge.builder()
            .setGraph(getActGraph())
            .setFactRecord(new FactRecord().setId(factId))
            .setFactType(FactTypeStruct.builder().setId(UUID.randomUUID()).setName("someType").build())
            .setInVertex(mock(Vertex.class))
            .setOutVertex(mock(Vertex.class))
            .build();

    assertNotNull(edge.id());
    assertSame(getActGraph(), edge.graph());
    assertEquals("someType", edge.label());
  }

  @Test
  public void testVerticesWithDirectionIn() {
    ObjectVertex destination = createVertex();
    ObjectVertex source = createVertex();

    Edge edge = FactEdge.builder()
            .setGraph(getActGraph())
            .setFactRecord(new FactRecord())
            .setFactType(FactTypeStruct.builder().build())
            .setInVertex(destination)
            .setOutVertex(source)
            .build();

    Iterator<Vertex> vertices = edge.vertices(Direction.IN);
    assertSame(destination.id(), vertices.next().id());
    assertFalse(vertices.hasNext());
  }

  @Test
  public void testVerticesWithDirectionOut() {
    ObjectVertex destination = createVertex();
    ObjectVertex source = createVertex();

    Edge edge = FactEdge.builder()
            .setGraph(getActGraph())
            .setFactRecord(new FactRecord())
            .setFactType(FactTypeStruct.builder().build())
            .setInVertex(destination)
            .setOutVertex(source)
            .build();

    Iterator<Vertex> vertices = edge.vertices(Direction.OUT);
    assertSame(source.id(), vertices.next().id());
    assertFalse(vertices.hasNext());
  }

  @Test
  public void testVerticesWithDirectionBoth() {
    ObjectVertex destination = createVertex();
    ObjectVertex source = createVertex();

    Edge edge = FactEdge.builder()
            .setGraph(getActGraph())
            .setFactRecord(new FactRecord())
            .setFactType(FactTypeStruct.builder().build())
            .setInVertex(destination)
            .setOutVertex(source)
            .build();

    Iterator<Vertex> vertices = edge.vertices(Direction.BOTH);
    assertSame(source.id(), vertices.next().id());
    assertSame(destination.id(), vertices.next().id());
    assertFalse(vertices.hasNext());
  }

  @Test
  public void testPropertiesOnlyFetchedOnce() {
    Edge edge = createEdge();

    assertEquals(list(edge.properties()), list(edge.properties()));
    verify(getPropertyHelper()).getFactProperties(notNull(), notNull());
  }

  @Test
  public void testPropertiesWithoutProperties() {
    Edge edge = createEdge();
    assertFalse(edge.properties().hasNext());
  }

  @Test
  public void testPropertiesWithoutMatchingProperty() {
    Edge edge = createEdge();
    assertFalse(edge.properties("something").hasNext());
  }

  @Test
  public void testPropertiesWithMatchingProperty() {
    when(getPropertyHelper().getFactProperties(any(), any()))
            .thenReturn(list(new PropertyEntry<>("value", "test")));

    Edge edge = createEdge();
    assertTrue(edge.properties("value").hasNext());
  }

  @Test
  public void testPropertiesWithMetaFacts() {
    when(getPropertyHelper().getFactProperties(any(), any())).thenReturn(list(
            new PropertyEntry<>("meta/tlp", "green"),
            new PropertyEntry<>("meta/observationTime", "2")));

    Edge edge = createEdge();
    Map<String, Property<Object>> props = MapUtils.map(edge.properties(), p -> MapUtils.Pair.T(p.key(), p));
    assertEquals("green", props.get("meta/tlp").value());
    assertEquals("2", props.get("meta/observationTime").value());
  }

  /* The following tests are adapted from gremlin-test EdgeTest. */

  @Test
  public void testValidateEquality() {
    Edge edge1 = createEdge();
    Edge edge2 = createEdge();

    assertEquals(edge1, edge1);
    assertEquals(edge2, edge2);
    assertNotEquals(edge1, edge2);
  }

  @Test
  public void testValidateIdEquality() {
    Edge edge1 = createEdge();
    Edge edge2 = createEdge();

    assertEquals(edge1.id(), edge1.id());
    assertEquals(edge2.id(), edge2.id());
    assertNotEquals(edge1.id(), edge2.id());
  }

  @Test
  public void testStandardStringRepresentation() {
    Edge edge = createEdge();
    assertEquals(StringFactory.edgeString(edge), edge.toString());
  }

  @Test
  public void testAutotypeStringProperties() {
    when(getPropertyHelper().getFactProperties(any(), any()))
            .thenReturn(list(new PropertyEntry<>("value", "value")));

    Edge edge = createEdge();
    String value = edge.value("value");
    assertEquals("value", value);
  }

  @Test
  public void testAutotypeLongProperties() {
    when(getPropertyHelper().getFactProperties(any(), any()))
            .thenReturn(list(new PropertyEntry<>("timestamp", 123456789L)));

    Edge edge = createEdge();
    long timestamp = edge.value("timestamp");
    assertEquals(123456789L, timestamp);
  }

  @Test
  public void testAutotypeFloatProperties() {
    when(getPropertyHelper().getFactProperties(any(), any()))
            .thenReturn(list(new PropertyEntry<>("trust", 0.3f)));

    Edge edge = createEdge();
    float trust = edge.value("trust");
    assertEquals(0.3f, trust, 0.0);
  }

  @Test
  public void testReturnEmptyPropertyIfKeyNonExistent() {
    Edge edge = createEdge();
    Property<Object> property = edge.property("something");
    assertEquals(Property.empty(), property);
  }

  @Test(expected = IllegalStateException.class)
  public void testGetValueThatIsNotPresentOnEdge() {
    Edge edge = createEdge();
    edge.value("something");
  }

  @Test
  public void testReturnOutThenInOnVertexIterator() {
    FactTypeStruct factType = FactTypeStruct.builder().setId(UUID.randomUUID()).setName("someFactType").build();
    FactRecord factRecord = new FactRecord()
            .setId(UUID.randomUUID())
            .setTypeID(factType.getId())
            .setValue("value")
            .setInReferenceToID(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            .setOrganizationID(UUID.fromString("00000000-0000-0000-0000-000000000002"))
            .setOriginID(UUID.fromString("00000000-0000-0000-0000-000000000003"))
            .setTrust(0.3f)
            .setConfidence(0.5f)
            .setAccessMode(FactRecord.AccessMode.Public)
            .setTimestamp(123456789L)
            .setLastSeenTimestamp(987654321L);

    ObjectVertex source = ObjectVertex.builder()
            .setGraph(getActGraph())
            .setObjectRecord(new ObjectRecord().setId(UUID.randomUUID()))
            .setObjectType(ObjectTypeStruct.builder().setId(UUID.randomUUID()).setName("someObjectType").build())
            .build();
    ObjectVertex destination = ObjectVertex.builder()
            .setGraph(getActGraph())
            .setObjectRecord(new ObjectRecord().setId(UUID.randomUUID()))
            .setObjectType(ObjectTypeStruct.builder().setId(UUID.randomUUID()).setName("someOtherObjectType").build())
            .build();

    FactEdge edge = FactEdge.builder()
            .setGraph(getActGraph())
            .setFactRecord(factRecord)
            .setFactType(factType)
            .setInVertex(destination)
            .setOutVertex(source)
            .build();

    assertEquals(source.id(), edge.outVertex().id());
    assertEquals(destination.id(), edge.inVertex().id());

    Iterator<Vertex> vertices = edge.vertices(Direction.BOTH);
    assertTrue(vertices.hasNext());
    assertEquals(source.id(), vertices.next().id());
    assertTrue(vertices.hasNext());
    assertEquals(destination.id(), vertices.next().id());
    assertFalse(vertices.hasNext());
  }

  private ObjectVertex createVertex() {
    return ObjectVertex.builder()
            .setGraph(getActGraph())
            .setObjectType(ObjectTypeStruct.builder().build())
            .setObjectRecord(new ObjectRecord().setId(UUID.randomUUID()))
            .build();
  }

  private Edge createEdge() {
    FactTypeStruct factType = FactTypeStruct.builder()
            .setId(UUID.randomUUID())
            .setName("someFactType")
            .build();

    FactRecord factRecord = new FactRecord()
            .setId(UUID.randomUUID())
            .setTypeID(factType.getId())
            .setValue("value")
            .setInReferenceToID(UUID.fromString("00000000-0000-0000-0000-000000000001"))
            .setOrganizationID(UUID.fromString("00000000-0000-0000-0000-000000000002"))
            .setOriginID(UUID.fromString("00000000-0000-0000-0000-000000000003"))
            .setAddedByID(UUID.fromString("00000000-0000-0000-0000-000000000004"))
            .setTrust(0.3f)
            .setConfidence(0.5f)
            .setAccessMode(FactRecord.AccessMode.Public)
            .setTimestamp(123456789L)
            .setLastSeenTimestamp(987654321L);

    ObjectTypeStruct objectType = ObjectTypeStruct.builder()
            .setId(UUID.randomUUID())
            .setName("someObjectType")
            .build();
    ObjectVertex source = ObjectVertex.builder()
            .setGraph(getActGraph())
            .setObjectRecord(
                    new ObjectRecord()
                            .setId(UUID.randomUUID())
                            .setValue("someObjectValue")
                            .setTypeID(objectType.getId()))
            .setObjectType(objectType)
            .build();

    ObjectVertex destination = ObjectVertex.builder()
            .setGraph(getActGraph())
            .setObjectRecord(
                    new ObjectRecord()
                            .setId(UUID.randomUUID())
                            .setValue("someOtherObjectValue")
                            .setTypeID(objectType.getId()))
            .setObjectType(objectType)
            .build();

    return FactEdge.builder()
            .setGraph(getActGraph())
            .setFactRecord(factRecord)
            .setFactType(factType)
            .setInVertex(destination)
            .setOutVertex(source)
            .build();
  }
}
