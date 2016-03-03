/*******************************************************************************
 *  Copyright FUJITSU LIMITED 2016 
 *******************************************************************************/

package org.oscm.billingservice.business.model.brokershare;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.BeforeClass;
import org.junit.Test;

import org.oscm.billingservice.business.XmlSearch;
import org.oscm.billingservice.business.model.brokershare.BrokerRevenueShareResult;
import org.oscm.billingservice.business.model.brokershare.BrokerShareResultAssembler;
import org.oscm.billingservice.business.model.brokershare.Currency;
import org.oscm.billingservice.business.model.brokershare.OrganizationData;
import org.oscm.billingservice.business.model.brokershare.Period;
import org.oscm.billingservice.business.model.brokershare.Service;
import org.oscm.billingservice.business.model.brokershare.Supplier;
import org.oscm.billingservice.dao.BillingDataRetrievalServiceLocal;
import org.oscm.billingservice.dao.SharesDataRetrievalServiceLocal;
import org.oscm.billingservice.service.BillingServiceBean;
import org.oscm.converter.XMLConverter;
import org.oscm.domobjects.BillingResult;
import org.oscm.domobjects.OrganizationHistory;
import org.oscm.domobjects.ProductHistory;
import org.oscm.domobjects.SubscriptionData;
import org.oscm.domobjects.SubscriptionHistory;
import org.oscm.domobjects.SupportedCurrency;

@SuppressWarnings("boxing")
public class BrokerShareResultAssemblerTest {

    private static List<BillingResult> billingResults;
    private static BrokerShareResultAssembler brokerShareAssembler;
    private static DatatypeFactory datatypeFactory;
    private static XmlSearch xmlSearch;

    // Map of subscriptionKey and testdata
    private static Map<Long, BillingResultDataMock> billingResultDataMock = new LinkedHashMap<Long, BillingResultDataMock>();

    private static final int NUMBER_SUPPLIERS = 2;
    private static final int NUMBER_SUBSCRIPTIONS = 6;
    private static final int NUMBER_CUSTOMER = 5;
    private static final Long BROKER_KEY = 36123785L;
    private static final String BROKER_ID = "brokerId";
    private static final String ORG_ADDRESS = "orgAddress_";
    private static final String ORG_NAME = "orgName_";
    private static final String ORG_EMAIL = "orgEmail_";
    private static final String ORG_ID = "orgId_";
    private static final long PERIOD_START_TIME = 78678647823L;
    private static final long PERIOD_END_TIME = 78678997823L;
    private static final String CURRENCY_EUR = "EUR";
    private static final String CURRENCY_USD = "USD";
    private static final String PRODUCT_ID1 = "productId_1";
    private static final String PRODUCT_ID2 = "productId_2";
    private static final String PRODUCT_ID3 = "productId_3";
    private static final boolean PRINT_TEST_DATA = false;

    SharesDataRetrievalServiceLocal dao = mock(SharesDataRetrievalServiceLocal.class);
    BillingDataRetrievalServiceLocal bdrs = mock(BillingDataRetrievalServiceLocal.class);
    BrokerShareResultAssembler assembler = new BrokerShareResultAssembler(dao,
            bdrs);

    static class BillingResultDataMock {
        String currencyCode;
        BigDecimal netAmount;
        ProductHistory product;

        public BillingResultDataMock(String currencyCode, BigDecimal netAmount,
                ProductHistory product) {
            this.currencyCode = currencyCode;
            this.netAmount = netAmount;
            this.product = product;
        }
    }

    @BeforeClass
    public static void setup() throws Exception {
        SharesDataRetrievalServiceLocal sharesRetrievalService = mock(SharesDataRetrievalServiceLocal.class);
        BillingDataRetrievalServiceLocal billingRetrievalService = mock(BillingDataRetrievalServiceLocal.class);
        brokerShareAssembler = spy(new BrokerShareResultAssembler(
                sharesRetrievalService, billingRetrievalService));

        // create test data
        createBillingResults();

        // mock service methods
        mockGetBrokerData();
        mockGetBrokerRevenueSharePercentage();
        mockGetOrgData(NUMBER_SUPPLIERS);
        mockGetOrgData(NUMBER_CUSTOMER);
        mockGetSupplierKeyForSubscription();
        mockGetProductHistoryData();
        mockGetSubscriptionHistoryData();
        mockGetBillingResults();
        mockXmlSearch();
        datatypeFactory = DatatypeFactory.newInstance();
    }

    private static void mockXmlSearch() {
        xmlSearch = mock(XmlSearch.class);
        doReturn(xmlSearch).when(brokerShareAssembler).newXmlSearch(
                any(BillingResult.class));
        Set<Long> pmKeys = new HashSet<Long>();
        pmKeys.add(Long.valueOf(0));
        doReturn(pmKeys).when(xmlSearch).findPriceModelKeys();
    }

    /**
     * Output of the first run
     */
    private static List<BillingResult> createBillingResults() {
        createBillingResultDataMock();

        // create Billing Result domain objects
        billingResults = new LinkedList<BillingResult>();
        for (long subscriptionKey = 1; subscriptionKey <= NUMBER_SUBSCRIPTIONS; subscriptionKey++) {
            BillingResult billingResult = new BillingResult();
            billingResult.setKey(10000 + subscriptionKey);
            billingResult.setSubscriptionKey(subscriptionKey);
            billingResult.setCurrency(new SupportedCurrency(
                    billingResultDataMock.get(subscriptionKey).currencyCode));
            billingResult.setNetAmount(billingResultDataMock
                    .get(subscriptionKey).netAmount);
            billingResults.add(billingResult);
        }
        return billingResults;
    }

    /**
     * Subscription;ProductId;ProductKey;Currency;NetAmount;Supplier <br/>
     * 1; productId_1; 1; EUR; 120; 1 <br/>
     * 2; productId_1; 1; EUR; 210.15; 1 <br/>
     * 3; productId_2; 2; EUR; 187.99; 1 <br/>
     * 4; productId_3; 3; EUR; 26; 2 <br/>
     * 5; productId_3; 3; USD; 523.56; 2 <br/>
     * 6; productId_1; 1; EUR; 176.44; 1 <br/>
     */
    private static void createBillingResultDataMock() {
        Long subscriptionKey = 1L;
        billingResultDataMock.put(subscriptionKey, new BillingResultDataMock(
                CURRENCY_EUR, BigDecimal.valueOf(120),
                getProductHistory(subscriptionKey)));

        subscriptionKey = 2L;
        billingResultDataMock.put(subscriptionKey, new BillingResultDataMock(
                CURRENCY_EUR, BigDecimal.valueOf(210.15),
                getProductHistory(subscriptionKey)));

        subscriptionKey = 3L;
        billingResultDataMock.put(subscriptionKey, new BillingResultDataMock(
                CURRENCY_EUR, BigDecimal.valueOf(187.99),
                getProductHistory(subscriptionKey)));

        subscriptionKey = 4L;
        billingResultDataMock.put(subscriptionKey, new BillingResultDataMock(
                CURRENCY_EUR, BigDecimal.valueOf(26),
                getProductHistory(subscriptionKey)));

        subscriptionKey = 5L;
        billingResultDataMock.put(subscriptionKey, new BillingResultDataMock(
                CURRENCY_USD, BigDecimal.valueOf(523.56),
                getProductHistory(subscriptionKey)));

        subscriptionKey = 6L;
        billingResultDataMock.put(subscriptionKey, new BillingResultDataMock(
                CURRENCY_EUR, BigDecimal.valueOf(176.44),
                getProductHistory(subscriptionKey)));

        printTestData();
    }

    private static void printTestData() {
        if (PRINT_TEST_DATA) {
            for (Long key : billingResultDataMock.keySet()) {
                BillingResultDataMock data = billingResultDataMock.get(key);
                System.out.println("Subscription : " + key + "\tproductId : "
                        + data.product.getDataContainer().getProductId()
                        + "\t\tproductKey : " + data.product.getObjKey()
                        + "\t\tcurrency : " + data.currencyCode
                        + "\t\toverallCosts : " + data.netAmount
                        + "\t\tsupplier : " + data.product.getVendorObjKey());
            }
        }
    }

    private static ProductHistory getProductHistory(long subscriptionKey) {
        ProductHistory product = new ProductHistory();

        String productId;
        long productKey;
        long templateKey;

        if (subscriptionKey == 1 || subscriptionKey == 2
                || subscriptionKey == 6) {
            productId = PRODUCT_ID1;
            productKey = 1;
            templateKey = 3000;
        } else if (subscriptionKey == 3) {
            productId = PRODUCT_ID2;
            productKey = 2;
            templateKey = 3001;
        } else if (subscriptionKey == 4 || subscriptionKey == 5) {
            productId = PRODUCT_ID3;
            productKey = 3;
            templateKey = 3002;
        } else {
            throw new IllegalArgumentException();
        }

        product.setObjKey(productKey);
        product.getDataContainer().setProductId(productId);
        product.setTemplateObjKey(templateKey);
        product.setVendorObjKey(getSupplierKey(subscriptionKey));
        return product;
    }

    private static long getSupplierKey(long subscriptionKey) {
        if ((subscriptionKey >= 1 && subscriptionKey <= 3)
                || (subscriptionKey == 6)) {
            return 1;
        } else {
            return 2;
        }
    }

    private static void mockGetBrokerData() {
        when(
                brokerShareAssembler.sharesRetrievalService
                        .loadLastOrganizationHistory(eq(BROKER_KEY)))
                .thenReturn(brokerOrganization());
        when(
                brokerShareAssembler.sharesRetrievalService
                        .getSupportedCountryCode(eq(BROKER_KEY))).thenReturn(
                "US");
    }

    private static void mockGetBrokerRevenueSharePercentage() {
        when(
                brokerShareAssembler.sharesRetrievalService
                        .loadBrokerRevenueSharePercentage(anyLong(), anyLong()))
                .thenReturn(BigDecimal.valueOf(10));
    }

    private static OrganizationHistory brokerOrganization() {
        OrganizationHistory org = new OrganizationHistory();
        org.setKey(BROKER_KEY);

        org.oscm.domobjects.OrganizationData orgData = new org.oscm.domobjects.OrganizationData();
        orgData.setOrganizationId(BROKER_ID);
        orgData.setAddress("address");
        orgData.setEmail("test@fujitsu.com");
        orgData.setName("Current Broker");
        org.setDataContainer(orgData);
        return org;
    }

    private static void mockGetSupplierKeyForSubscription() {
        for (long subscriptionKey = 1; subscriptionKey <= NUMBER_SUBSCRIPTIONS; subscriptionKey++) {
            when(
                    brokerShareAssembler.billingRetrievalService
                            .loadSupplierKeyForSubscription(eq(subscriptionKey)))
                    .thenReturn(
                            billingResultDataMock.get(Long
                                    .valueOf(subscriptionKey)).product
                                    .getVendorObjKey());
        }
    }

    private static void mockGetProductHistoryData() {
        for (long subscriptionKey = 1; subscriptionKey <= NUMBER_SUBSCRIPTIONS; subscriptionKey++) {
            when(
                    brokerShareAssembler.sharesRetrievalService
                            .loadProductOfVendor(eq(subscriptionKey),
                                    anyLong(), anyLong()))
                    .thenReturn(
                            billingResultDataMock.get(Long
                                    .valueOf(subscriptionKey)).product);
        }
    }

    private static void mockGetSubscriptionHistoryData() {
        for (long subscriptionKey = 1; subscriptionKey <= NUMBER_SUBSCRIPTIONS; subscriptionKey++) {
            when(
                    brokerShareAssembler.sharesRetrievalService
                            .loadSubscriptionHistoryWithinPeriod(
                                    eq(subscriptionKey), eq(PERIOD_END_TIME)))
                    .thenReturn(subscriptionHistory(subscriptionKey));
        }
    }

    private static void mockGetOrgData(int number) {
        for (long orgKey = 1; orgKey <= number; orgKey++) {
            when(
                    brokerShareAssembler.sharesRetrievalService
                            .loadLastOrganizationHistory(eq(orgKey)))
                    .thenReturn(getOrganizationData(orgKey));
            when(
                    brokerShareAssembler.sharesRetrievalService
                            .getSupportedCountryCode(anyLong())).thenReturn(
                    "US");
        }
    }

    private static void mockGetBillingResults() {
        when(
                brokerShareAssembler.sharesRetrievalService
                        .loadBillingResultsForBroker(anyLong(), anyLong(),
                                anyLong())).thenReturn(billingResults);
    }

    private static SubscriptionHistory subscriptionHistory(long subscriptionKey) {
        SubscriptionHistory subscription = new SubscriptionHistory();
        subscription.setKey(subscriptionKey);
        if (subscriptionKey == 6) {
            subscription.setOrganizationObjKey(1);
        } else {
            subscription.setOrganizationObjKey(subscriptionKey);
        }
        SubscriptionData dataContainer = new SubscriptionData();
        dataContainer.setSubscriptionId(Long.valueOf(subscriptionKey)
                .toString());
        subscription.setDataContainer(dataContainer);
        return subscription;
    }

    private static OrganizationHistory getOrganizationData(long orgKey) {

        OrganizationHistory organization = new OrganizationHistory();
        org.oscm.domobjects.OrganizationData organizationData = organization
                .getDataContainer();
        organizationData.setAddress(ORG_ADDRESS + orgKey);
        organizationData.setName(ORG_NAME + orgKey);
        organizationData.setEmail(ORG_EMAIL + orgKey);
        organizationData.setOrganizationId(ORG_ID + orgKey);
        organizationData.setLocale("GB");
        return organization;
    }

    @Test(expected = NullPointerException.class)
    public void build_BrokerIdIsNull() throws Exception {
        // when
        brokerShareAssembler.build(null, PERIOD_START_TIME, PERIOD_END_TIME);
    }

    @Test
    public void build_verifyBrokerData() throws Exception {
        // when
        BrokerRevenueShareResult result = brokerShareAssembler.build(
                BROKER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);

        // then
        assertNotNull(result);
        assertNotNull(result.getOrganizationKey());
        assertEquals(BROKER_KEY.longValue(), result.getOrganizationKey()
                .longValue());
        assertNotNull(result.getOrganizationId());
        assertEquals(BROKER_ID, result.getOrganizationId());
    }

    @Test
    public void build_period() throws Exception {
        // when
        BrokerRevenueShareResult result = brokerShareAssembler.build(
                BROKER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);

        // then
        Period period = result.getPeriod();
        assertNotNull(period);

        // start date
        assertNotNull(period.getStartDate());
        assertEquals(PERIOD_START_TIME, period.getStartDate().longValue());
        assertNotNull(period.getStartDateIsoFormat());
        assertEquals(getXMLGregorianCalendar(PERIOD_START_TIME),
                period.getStartDateIsoFormat());

        // end date
        assertNotNull(period.getEndDate());
        assertEquals(PERIOD_END_TIME, period.getEndDate().longValue());
        assertNotNull(period.getEndDateIsoFormat());
        assertEquals(getXMLGregorianCalendar(PERIOD_END_TIME),
                period.getEndDateIsoFormat());
    }

    private XMLGregorianCalendar getXMLGregorianCalendar(long time) {
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTimeInMillis(time);
        return datatypeFactory.newXMLGregorianCalendar(gc);
    }

    @Test
    public void build_currency() throws Exception {
        // when
        BrokerRevenueShareResult result = brokerShareAssembler.build(
                BROKER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);

        // then
        assertNotNull(result.getCurrency());
        Set<String> currencies = getBillingResultCurrencies();
        assertEquals(currencies.size(), result.getCurrency().size());
        for (Currency currency : result.getCurrency()) {
            assertTrue(currencies.contains(currency.getId()));
        }
    }

    private Set<String> getBillingResultCurrencies() {
        Set<String> result = new HashSet<String>();
        for (BillingResult billingResult : billingResults) {
            result.add(billingResult.getCurrencyCode());
        }
        return result;
    }

    @Test
    public void build_supplier() throws Exception {
        // when
        BrokerRevenueShareResult result = brokerShareAssembler.build(
                BROKER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);

        // then
        // verify supplier existence
        for (Currency currency : result.getCurrency()) {
            if (currency.getId().equals(CURRENCY_EUR)) {
                assertEquals(2, currency.getSupplier().size());

            } else if (currency.getId().equals(CURRENCY_USD)) {
                assertEquals(1, currency.getSupplier().size());
            }

            for (Supplier supplier : currency.getSupplier()) {
                verifyOrganizationData(supplier.getOrganizationData());
            }
        }
    }

    private void verifyOrganizationData(OrganizationData supplierDataToVerify) {
        assertNotNull(supplierDataToVerify);
        assertNotNull(supplierDataToVerify.getKey());

        OrganizationHistory supplierTestdata = getOrganizationData(supplierDataToVerify
                .getKey().longValue());

        assertNotNull(supplierDataToVerify.getId());
        assertEquals(supplierTestdata.getOrganizationId(),
                supplierDataToVerify.getId());
        assertNotNull(supplierDataToVerify.getEmail());
        assertEquals(supplierTestdata.getEmail(),
                supplierDataToVerify.getEmail());
        assertNotNull(supplierDataToVerify.getName());
        assertEquals(supplierTestdata.getOrganizationName(),
                supplierDataToVerify.getName());
        assertNotNull(supplierDataToVerify.getAddress());
        assertEquals(supplierTestdata.getAddress(),
                supplierDataToVerify.getAddress());
    }

    @Test
    public void build_service() throws Exception {
        // when
        BrokerRevenueShareResult result = brokerShareAssembler.build(
                BROKER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);

        // then
        for (Currency currency : result.getCurrency()) {
            for (Supplier supplier : currency.getSupplier()) {
                long supplierKey = supplier.getOrganizationData().getKey()
                        .longValue();
                if (supplierKey == 1) {
                    // 2 subscriptions of same product and a third one
                    assertEquals(2, supplier.getService().size());
                    verifyServiceData(supplier.getService().get(0), PRODUCT_ID1);
                    verifyServiceData(supplier.getService().get(1), PRODUCT_ID2);

                } else if (supplierKey == 2) {
                    // 2 subscriptions of same product
                    assertEquals(1, supplier.getService().size());
                    verifyServiceData(supplier.getService().get(0), PRODUCT_ID3);
                }
            }
        }
    }

    private void verifyServiceData(Service serviceToVerify,
            String expectedProductId) {
        boolean verified = false;
        for (BillingResultDataMock testdata : billingResultDataMock.values()) {
            if (testdata.product.getDataContainer().getProductId()
                    .equals(expectedProductId)) {
                compareServiceData(serviceToVerify, testdata.product);
                verified = true;
                break;
            }
        }
        assertTrue(verified);
    }

    private void compareServiceData(Service serviceToVerify,
            ProductHistory product) {
        assertNotNull(serviceToVerify);
        assertNotNull(serviceToVerify.getId());
        assertEquals(product.getDataContainer().getProductId(),
                serviceToVerify.getId());
        assertNotNull(serviceToVerify.getKey());
        assertEquals(product.getObjKey(), serviceToVerify.getKey().longValue());
        assertNotNull(serviceToVerify.getTemplateKey());
        assertEquals(product.getTemplateObjKey().longValue(), serviceToVerify
                .getTemplateKey().longValue());
    }

    @Test
    public void build_serviceRevenue() throws Exception {
        // when
        BrokerRevenueShareResult result = brokerShareAssembler.build(
                BROKER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);
        result.calculateAllShares();

        // then
        for (Currency currency : result.getCurrency()) {
            for (Supplier supplier : currency.getSupplier()) {
                for (Service service : supplier.getService()) {
                    assertNotNull(service.getServiceRevenue());
                    assertNotNull(service.getServiceRevenue().getTotalAmount());

                    BigDecimal expected = getExpectedBaseRevenue(
                            currency.getId(), supplier.getOrganizationData()
                                    .getKey().longValue(), service.getId());

                    assertEquals(0, service.getServiceRevenue()
                            .getTotalAmount().compareTo(expected));

                    assertNotNull(service.getServiceRevenue()
                            .getBrokerRevenueSharePercentage());
                    assertNotNull(service.getServiceRevenue()
                            .getBrokerRevenue());
                }
            }
        }
    }

    private BigDecimal getExpectedBaseRevenue(String currencyCode,
            long supplierKey, String serviceId) {
        BigDecimal result = BigDecimal.ZERO;
        for (BillingResultDataMock testdata : billingResultDataMock.values()) {
            if (testdata.currencyCode.equals(currencyCode)
                    && testdata.product.getVendorObjKey() == supplierKey
                    && testdata.product.getDataContainer().getProductId()
                            .equals(serviceId)) {
                result = result.add(testdata.netAmount);
            }
        }
        return result;
    }

    @Test
    public void build_supplierRevenue() throws Exception {
        // when
        BrokerRevenueShareResult result = brokerShareAssembler.build(
                BROKER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);
        result.calculateAllShares();

        // then
        for (Currency currency : result.getCurrency()) {
            for (Supplier supplier : currency.getSupplier()) {
                assertNotNull(supplier.getBrokerRevenuePerSupplier());
                assertNotNull(supplier.getBrokerRevenuePerSupplier()
                        .getAmount());

                BigDecimal supplierRevenue = BigDecimal.ZERO;
                for (Service service : supplier.getService()) {
                    supplierRevenue = supplierRevenue.add(service
                            .getServiceRevenue().getBrokerRevenue());
                }
                assertEquals(0, supplier.getBrokerRevenuePerSupplier()
                        .getAmount().compareTo(supplierRevenue));
            }
        }
    }

    @Test
    public void build_revenue() throws Exception {
        // when
        BrokerRevenueShareResult result = brokerShareAssembler.build(
                BROKER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);
        result.calculateAllShares();

        // then
        for (Currency currency : result.getCurrency()) {
            assertNotNull(currency.getBrokerRevenue());
            assertNotNull(currency.getBrokerRevenue().getAmount());

            BigDecimal revenue = BigDecimal.ZERO;
            for (Supplier supplier : currency.getSupplier()) {
                revenue = revenue.add(supplier.getBrokerRevenuePerSupplier()
                        .getAmount());
            }
            assertEquals(0,
                    revenue.compareTo(currency.getBrokerRevenue().getAmount()));
        }
    }

    @Test
    public void serialize() throws Exception {
        // given
        BrokerRevenueShareResult result = brokerShareAssembler.build(
                BROKER_KEY, PERIOD_START_TIME, PERIOD_END_TIME);
        result.calculateAllShares();

        // when
        JAXBContext jc = JAXBContext
                .newInstance(BrokerRevenueShareResult.class);
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        marshaller.marshal(result, bos);
        assertNotNull(bos.toByteArray());
        printXml(bos);

        final List<String> fragments = new ArrayList<String>();
        fragments.add(new String(bos.toByteArray(), "UTF-8"));

        byte[] xmlBytes = XMLConverter.combine("RevenueSharesResults",
                fragments, BrokerRevenueShareResult.SCHEMA);
        ByteArrayOutputStream bos1 = new ByteArrayOutputStream();
        bos1.write(xmlBytes);
        System.out.println(new String(bos1.toByteArray()));

        SchemaFactory schemaFactory = SchemaFactory
                .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        URL schemaUrl = BillingServiceBean.class.getResource("/"
                + "BrokerRevenueShareResult.xsd");
        Schema schema = schemaFactory.newSchema(schemaUrl);

        Source xmlFile = new StreamSource(new ByteArrayInputStream(xmlBytes));
        Validator validator = schema.newValidator();
        validator.validate(xmlFile);

    }

    private void printXml(ByteArrayOutputStream bos) {
        if (PRINT_TEST_DATA) {
            System.out.println(new String(bos.toByteArray()));
        }
    }

    @Test
    public void buildService_cleanProductId() {

        // given
        ProductHistory givenProduct = new ProductHistory();
        givenProduct.setObjKey(7);
        givenProduct.setTemplateObjKey(Long.valueOf(8));
        givenProduct.getDataContainer().setProductId("prodId#645646");

        // when
        Service constructedSerive = brokerShareAssembler
                .buildService(givenProduct);

        // then
        assertEquals("prodId", constructedSerive.getId());
    }

    /**
     * The period start and end times are UTC times
     * 
     * @throws Exception
     */
    @Test
    public void setPeriod() throws Exception {

        // given
        long givenStartPeriod = 0;
        long givenEndPeriod = 1347867425534L;

        // when
        BrokerRevenueShareResult result = new BrokerRevenueShareResult();
        assembler.result = result;
        assembler.setPeriod(givenStartPeriod, givenEndPeriod);

        // then
        Period period = assembler.result.getPeriod();
        assertEquals(givenStartPeriod, period.getStartDate().longValue());
        assertEquals("1970-01-01T00:00:00.000Z", period.getStartDateIsoFormat()
                .toString());
        assertEquals(givenEndPeriod, period.getEndDate().longValue());
        assertEquals("2012-09-17T07:37:05.534Z", period.getEndDateIsoFormat()
                .toString());
    }
}
