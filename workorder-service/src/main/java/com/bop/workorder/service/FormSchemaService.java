package com.bop.workorder.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bop.workorder.common.BizException;
import com.bop.workorder.entity.FormTemplate;
import com.bop.workorder.enums.BizType;
import com.bop.workorder.mapper.FormTemplateMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FormSchemaService {

    private final FormTemplateMapper formTemplateMapper;
    private final ObjectMapper objectMapper;
    private final JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    public FormSchemaService(FormTemplateMapper formTemplateMapper, ObjectMapper objectMapper) {
        this.formTemplateMapper = formTemplateMapper;
        this.objectMapper = objectMapper;
    }

    public FormTemplate requireTemplate(String formCode, Integer schemaVersion) {
        FormTemplate template = formTemplateMapper.selectOne(new LambdaQueryWrapper<FormTemplate>()
                .eq(FormTemplate::getFormCode, formCode)
                .eq(FormTemplate::getSchemaVersion, schemaVersion));
        if (template == null) {
            throw new BizException(20002, "Form template not found: " + formCode + " v" + schemaVersion);
        }
        return template;
    }

    public BigDecimal validateAndCalculate(BizType bizType, Integer schemaVersion, String extJson) {
        if (!StringUtils.hasText(extJson)) {
            throw new BizException(20002, "extJson is required");
        }
        FormTemplate template = requireTemplate(bizType.name(), schemaVersion);
        JsonNode rootNode;
        try {
            rootNode = objectMapper.readTree(extJson);
        } catch (Exception ex) {
            throw new BizException(20002, "Invalid extJson syntax", ex);
        }

        JsonSchema schema = schemaFactory.getSchema(template.getSchemaJson());
        Set<ValidationMessage> errors = schema.validate(rootNode);
        if (!errors.isEmpty()) {
            String detail = errors.stream()
                    .map(ValidationMessage::getMessage)
                    .collect(Collectors.joining("; "));
            throw new BizException(20002, "Form schema validation failed: " + detail);
        }

        return calculateAmount(bizType, rootNode);
    }

    private BigDecimal calculateAmount(BizType bizType, JsonNode rootNode) {
        BigDecimal total = BigDecimal.ZERO;
        if (bizType == BizType.WORKHOUR) {
            // 金额以单价与人天独立重算，不信任前端传入的 subtotal
            JsonNode tiers = rootNode.get("priceTiers");
            if (tiers != null && tiers.isArray()) {
                for (JsonNode tier : tiers) {
                    BigDecimal unitPrice = new BigDecimal(tier.get("unitPrice").asText());
                    BigDecimal personDays = new BigDecimal(tier.get("personDays").asText());
                    BigDecimal tierAmount = unitPrice.multiply(personDays).setScale(2, RoundingMode.HALF_UP);
                    total = total.add(tierAmount);
                }
            }
            return total.setScale(2, RoundingMode.HALF_UP);
        }

        if (bizType == BizType.ASSESSMENT) {
            JsonNode items = rootNode.get("deductionItems");
            if (items != null && items.isArray()) {
                for (JsonNode item : items) {
                    BigDecimal minusAmount = new BigDecimal(item.get("minusAmount").asText());
                    total = total.add(minusAmount);
                }
            }
            return total.setScale(2, RoundingMode.HALF_UP);
        }

        throw new BizException(20002, "Unsupported bizType: " + bizType);
    }
}
