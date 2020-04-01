package local.happysixplus.backendcodeanalysis.po;

import java.util.Set;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 项目包括源代码，初始图的结构等静态信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(indexes = { @Index(columnList = "userId") })
public class ProjectPo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    Long userId;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn()
    Set<VertexPo> vertices;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn()
    Set<EdgePo> edges;

}