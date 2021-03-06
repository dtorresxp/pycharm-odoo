package dev.ngocta.pycharm.odoo.data;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementResolveResult;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.ResolveResult;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ArrayUtil;
import com.intellij.util.PlatformIcons;
import com.jetbrains.python.psi.PyUtil;
import dev.ngocta.pycharm.odoo.module.OdooModule;
import dev.ngocta.pycharm.odoo.module.OdooModuleUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

public class OdooExternalIdReference extends PsiReferenceBase.Poly<PsiElement> {
    private final Computable<String[]> myModelsResolver;
    private final OdooRecordSubType mySubType;
    private final boolean myAllowRelative;

    public OdooExternalIdReference(@NotNull PsiElement element,
                                   @Nullable TextRange rangeInElement,
                                   @Nullable Computable<String[]> modelsResolver,
                                   @Nullable OdooRecordSubType subType,
                                   boolean allowRelative) {
        super(element, rangeInElement, false);
        myModelsResolver = modelsResolver;
        mySubType = subType;
        myAllowRelative = allowRelative;
    }

    @NotNull
    @Override
    public ResolveResult[] multiResolve(boolean incompleteCode) {
        return PsiElementResolveResult.createResults(resolveInner());
    }

    @NotNull
    protected List<PsiElement> resolveInner() {
        return PyUtil.getParameterizedCachedValue(getElement(), null, param -> {
            PsiElement element = getElement();
            String id = getValue();
            if (!id.contains(".") && myAllowRelative) {
                OdooModule module = OdooModuleUtils.getContainingOdooModule(element);
                if (module != null) {
                    id = module.getName() + "." + id;
                }
            }
            Project project = element.getProject();
            List<OdooRecord> records = OdooExternalIdIndex.findRecordsById(id, element);
            List<PsiElement> elements = new LinkedList<>();
            records.forEach(record -> elements.addAll(record.getElements(project)));
            return elements;
        });
    }

    @NotNull
    private String[] getModels() {
        if (myModelsResolver == null) {
            return new String[0];
        }
        String[] models = myModelsResolver.compute();
        return models != null ? models : new String[0];
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        OdooModule module = OdooModuleUtils.getContainingOdooModule(getElement());
        if (module == null) {
            return new Object[0];
        }
        List<LookupElement> elements = new LinkedList<>();
        GlobalSearchScope scope = module.getSearchScope(true);
        String[] models = getModels();
        Project project = getElement().getProject();
        OdooExternalIdIndex.processAllRecords(project, scope, record -> {
            if ((models.length == 0 || ArrayUtil.contains(record.getModel(), models))
                    && (mySubType == null || mySubType == record.getSubType())) {
                List<String> ids = new LinkedList<>();
                ids.add(record.getId());
                if (myAllowRelative && record.getModule().equals(module.getName())) {
                    ids.add(record.getName());
                }
                ids.forEach(id -> {
                    LookupElement element = LookupElementBuilder.create(id)
                            .withTypeText(record.getModel())
                            .withIcon(PlatformIcons.XML_TAG_ICON);
                    elements.add(element);
                });
            }
            return true;
        });
        return elements.toArray();
    }
}
