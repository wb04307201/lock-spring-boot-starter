package cn.wubo.lock.config;

import cn.wubo.lock.core.LockAliasProperties;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "lock")
public class LockProperties {
    List<LockAliasProperties> config = new ArrayList<>();
}
