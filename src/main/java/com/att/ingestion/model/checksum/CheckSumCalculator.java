package com.att.ingestion.model.checksum;

import java.util.Collection;
import java.util.Formatter;
import java.util.Iterator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;

public class CheckSumCalculator
{

    private Formatter checkSumReport = new Formatter();
    private int checksum;
    private final boolean bWithReport;

    public CheckSumCalculator(boolean bWithReport)
    {
        this(bWithReport, 0);
    }

    public CheckSumCalculator(boolean bWithReport, int checksum)
    {
        this.checkSumReport = new Formatter();
        this.bWithReport = bWithReport;
        this.checksum = checksum;
        if (bWithReport)
        {
            checkSumReport.format("==================  Checksum Report ===================\n");
        }
    }

    public void add(Object obj, String name)
    {
        int hash = hashCode(obj, name);
        checksum ^= hash;

        if (bWithReport)
        {
            if (obj != null)
            {
                checkSumReport.format("-- %s value [%s] class [%s] sum [%d] checksum [%d]\n",
                        name,
                        obj,
                        obj.getClass().getCanonicalName(),
                        hash,
                        checksum);
            } else
            {
                checkSumReport.format("-- %s value NULL sum [%d] checksum [%d]\n",
                        name,
                        hash,
                        checksum);
            }
        }
    }

    @SuppressWarnings("rawtypes")
	private int hashCode(Object obj, String name)
    {
        int hash = 0;
        if (obj != null)
        {
            if (obj instanceof Number)
            {
                Double doubleValue = ((Number) obj).doubleValue();
                hash = doubleValue.hashCode();
            }
            else if (obj instanceof Enum)
            {
                hash = ((Enum) obj).name().hashCode();
            }
            else if (obj instanceof String)
            {
                String value = obj.toString();
                hash = value.hashCode();
            }
            else if (obj instanceof JsonNode)
            {
                JsonNode node = (JsonNode) obj;
                if(node.isContainerNode())
                {
                    Iterator<String> fieldIterator = node.fieldNames();
                    while (fieldIterator.hasNext())
                    {
                        String fieldName = fieldIterator.next();
                        JsonNode fieldValue =  node.get(fieldName);
                        //special handling for double nodes only
                        if(fieldValue.isDouble())
                        {
                            fieldValue = handleNumericNode(fieldValue);
                        }


                        String nameExtended = String.format("%s fieldName [%s]", name, fieldName);
                        add(fieldValue, nameExtended);
                    }
                }
                else
                {
                    //BooleanNode does not override hashcode
                    // default hashcode impl is used
                    // this results in different hashcode on different servers
                    if(node instanceof BooleanNode)
                    {
                        Boolean bool = node.asBoolean();
                        hash = bool.hashCode();
                    }
                    else
                    {
                        hash = node.hashCode();
                    }
                }
            }
            else if (obj instanceof Collection)
            {
                Collection col = (Collection) obj;
                if (!col.isEmpty())
                {
                    hash = col.hashCode();
                }
            } else
            {
                hash = obj.hashCode();
            }
        }
        return hash;
    }

    public int getChecksum()
    {
        return checksum;
    }

    public String getReport()
    {
        return checkSumReport.toString();
    }


    private JsonNode handleNumericNode(JsonNode fieldValue)
    {
        double dbl = fieldValue.asDouble();
        if ((dbl == Math.floor(dbl)) && !Double.isInfinite(dbl) && dbl < Integer.MAX_VALUE && dbl > Integer.MIN_VALUE)
        {
            //integer
            return new IntNode(fieldValue.asInt());
        }
        else
        {
            return fieldValue;
        }
    }

}
