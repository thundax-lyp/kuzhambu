package com.thundax.kuzhambu.storage.application.service.command;

import com.thundax.kuzhambu.storage.domain.model.entity.StoredObjectReference;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AddStorageReferencesCommand {
    private List<StoredObjectReference> references;
}
