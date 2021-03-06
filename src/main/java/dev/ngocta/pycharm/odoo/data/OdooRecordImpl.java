package dev.ngocta.pycharm.odoo.data;

import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlFile;
import com.jetbrains.python.psi.PyClass;
import com.jetbrains.python.psi.PyElement;
import com.jetbrains.python.psi.PyFile;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.model.OdooModelInfo;
import dev.ngocta.pycharm.odoo.model.OdooModelUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class OdooRecordImpl implements OdooRecord {
    private final String myName;
    private final String myModel;
    private final String myModule;
    private final OdooRecordSubType mySubType;
    private final VirtualFile myDataFile;

    public OdooRecordImpl(@NotNull String name,
                          @NotNull String model,
                          @NotNull String module,
                          @Nullable OdooRecordSubType subType,
                          @Nullable VirtualFile dataFile) {
        myName = name;
        myModel = model;
        myModule = module;
        mySubType = subType;
        myDataFile = dataFile;
    }

    public OdooRecordImpl(@NotNull String id,
                          @NotNull String model,
                          @Nullable OdooRecordSubType subType,
                          @NotNull String containingModule,
                          @Nullable VirtualFile dataFile) {
        String name, module;
        String[] splits = id.split("\\.", 2);
        if (splits.length == 1) {
            name = splits[0];
            module = containingModule;
        } else {
            name = splits[1];
            module = splits[0];
        }
        myName = name;
        myModel = model;
        myModule = module;
        mySubType = subType;
        myDataFile = dataFile;
    }

    @NotNull
    public String getName() {
        return myName;
    }

    @NotNull
    @Override
    public String getModel() {
        return myModel;
    }

    @NotNull
    public String getModule() {
        return myModule;
    }

    @NotNull
    public String getId() {
        return getModule() + "." + getName();
    }

    @Nullable
    public OdooRecordSubType getSubType() {
        return mySubType;
    }

    @Nullable
    @Override
    public VirtualFile getDataFile() {
        return myDataFile;
    }

    @Override
    public List<PsiElement> getElements(@NotNull Project project) {
        if (myDataFile == null || !myDataFile.isValid()) {
            return Collections.emptyList();
        }
        if (OdooDataUtils.isCsvFile(myDataFile)) {
            return Collections.singletonList(new OdooCsvRecord(myDataFile, project, getId()));
        }
        PsiFile file = PsiManager.getInstance(project).findFile(myDataFile);
        if (file == null) {
            return Collections.emptyList();
        }
        List<PsiElement> result = PyUtil.getParameterizedCachedValue(file, this, param -> {
            List<PsiElement> elements = new LinkedList<>();
            if (file instanceof XmlFile) {
                OdooDomRoot root = OdooDataUtils.getDomRoot((XmlFile) file);
                if (root != null) {
                    List<OdooDomRecordLike> records = root.getAllRecordLikeItems();
                    for (OdooDomRecordLike record : records) {
                        if (this.equals(record.getRecord())) {
                            XmlElement xmlElement = record.getXmlElement();
                            if (xmlElement != null) {
                                elements.add(xmlElement);
                            }
                        }
                    }
                }
            } else if (file instanceof PyFile) {
                List<PyClass> classes = ((PyFile) file).getTopLevelClasses();
                for (PyClass cls : classes) {
                    OdooModelInfo info = OdooModelInfo.getInfo(cls);
                    if (info != null && OdooModelUtils.getIrModelRecordName(info.getName()).equals(myName)) {
                        elements.add(cls);
                    }
                }
            }
            return elements;
        });
        return Collections.unmodifiableList(result);
    }

    public List<NavigationItem> getNavigationItems(@NotNull Project project) {
        return getElements(project).stream()
                .map(element -> {
                    if (element instanceof PyElement) {
                        return (PyElement) element;
                    } else {
                        return new OdooRecordNavigationItem(this, element);
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OdooRecordImpl that = (OdooRecordImpl) o;
        return myName.equals(that.myName) &&
                myModel.equals(that.myModel) &&
                myModule.equals(that.myModule) &&
                mySubType == that.mySubType &&
                Objects.equals(myDataFile, that.myDataFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(myName, myModel, myModule, mySubType, myDataFile);
    }

    public OdooRecord withDataFile(VirtualFile file) {
        return new OdooRecordImpl(myName, myModel, myModule, mySubType, file);
    }

    public OdooRecord withoutDataFile() {
        return new OdooRecordImpl(myName, myModel, myModule, mySubType, null);
    }
}
