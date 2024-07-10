package au.edu.sydney.brawndo.erp.spfea;

import au.edu.sydney.brawndo.erp.auth.AuthModule;
import au.edu.sydney.brawndo.erp.auth.AuthToken;
import au.edu.sydney.brawndo.erp.database.TestDatabase;
import au.edu.sydney.brawndo.erp.ordering.Customer;
import au.edu.sydney.brawndo.erp.ordering.Order;
import au.edu.sydney.brawndo.erp.ordering.Product;
import au.edu.sydney.brawndo.erp.spfea.products.ProductDatabase;
import au.edu.sydney.brawndo.erp.spfea.products.ProductImpl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.InjectMocks;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat; 
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.doNothing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import org.mockito.Mockito;



@ExtendWith(MockitoExtension.class)
public class AllowedScopeTest {

    private MockedStatic<TestDatabase> mockedStaticDB;
    private TestDatabase mockedDB;

    @InjectMocks
    private SPFEAFacade facade;
    private AuthToken mockedToken;

    private Product mockedProd100;
    private Product mockedProd300;

    @AfterEach
    void tearDown() {
        mockedStaticDB.close();
    }

    @BeforeEach
    public void setup() {
        // We need to stick an instance in the static class here, so we revert to native Mockito
        mockedDB = mock(TestDatabase.class);
        mockedStaticDB = mockStatic(TestDatabase.class);
        when(TestDatabase.getInstance()).thenReturn(mockedDB);

        try (MockedStatic<AuthModule> authModuleMock = mockStatic(AuthModule.class)) {
        }

        mockedProd100 = mock(Product.class);
        Mockito.lenient().when(mockedProd100.getCost()).thenReturn(100.0);
        Mockito.lenient().when(mockedProd100.getProductName()).thenReturn("Fake Product");

        mockedProd300 = mock(Product.class);
        Mockito.lenient().when(mockedProd300.getCost()).thenReturn(300.0);
        Mockito.lenient().when(mockedProd300.getProductName()).thenReturn("Fake Product 2");

        facade = new SPFEAFacade();
    }

    private void setupLogin() {
        try (MockedStatic<AuthModule> authModuleMock = mockStatic(AuthModule.class)) {
        mockedToken = mock(AuthToken.class);
        when(AuthModule.login("username", "password")).thenReturn(mockedToken);
        when(AuthModule.authenticate(mockedToken)).thenReturn(true); // This works for cross-use like db or contact

        facade.login("username", "password");
        }
    }

    @Test
    public void login() {
        try (MockedStatic<AuthModule> authModuleMock = mockStatic(AuthModule.class)) {
        when(AuthModule.login("username", "password")).thenReturn(mock(AuthToken.class));

        assertTrue(facade.login("username", "password"));
        authModuleMock.verify(() ->
        AuthModule.login("username", "password"));

        assertFalse(facade.login("something else", "password"));
        authModuleMock.verify(() ->
        AuthModule.login("something else", "password"));
        }
    }

    @Test
    public void getAllOrders() {
        boolean thrown = false;
        try {
            facade.getAllOrders();
        } catch (SecurityException ignored) {
            thrown = true;
        }

        assertTrue(thrown);

        setupLogin();

        Order mockedOrder1 = mock(Order.class);
        when(mockedOrder1.getOrderID()).thenReturn(1001);
        Order mockedOrder2 = mock(Order.class);
        when(mockedOrder2.getOrderID()).thenReturn(2002);
        when(mockedDB.getOrders(mockedToken)).thenReturn(Arrays.asList(mockedOrder1, mockedOrder2));

        List<Integer> result = facade.getAllOrders();
        assertEquals(2, result.size());
        assertTrue(result.contains(1001));
        assertTrue(result.contains(2002));

        verify(mockedDB).getOrders(mockedToken);
    }

    @Test
    public void createOrder() {
        boolean thrown = false;
        try {
            facade.createOrder(0, LocalDateTime.now(), false, false, 0, 0, 0, 0);
        } catch (SecurityException ignored) {
            thrown = true;
        }

        assertTrue(thrown);

        setupLogin();

        when(mockedDB.getCustomerIDs(mockedToken)).thenReturn(Arrays.asList(1, 2, 3));

        thrown = false;
        try {
            facade.createOrder(-1, LocalDateTime.now(), false, false, 0, 0, 0, 0);
        } catch (IllegalArgumentException ignore) {
            thrown = true;
        }

        assertTrue(thrown, "Accepts invalid Customer ID");

        Integer testOrderID = facade.createOrder(1, LocalDateTime.now(), false, false, 0, 0, 0, 0);
        assertNull(testOrderID, "Accepts invalid discountType");
        testOrderID = facade.createOrder(1, LocalDateTime.now(), false, false, 3, 0, 0, 0);
        assertNull(testOrderID, "Accepts invalid discountType");
    }

    @Test
    public void testOrderBusinessBulk() {
        setupLogin();
        when(mockedDB.getCustomerIDs(mockedToken)).thenReturn(Arrays.asList(1, 2, 3));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        doNothing().when(mockedDB).saveOrder(eq(mockedToken), captor.capture());

        int testOrderID = facade.createOrder(2, LocalDateTime.now(), true, false, 2, 10, 20, 10);
        facade.logout();

        verify(mockedDB).saveOrder(eq(mockedToken), any());
        Order order = captor.getValue();
        assertEquals(testOrderID, order.getOrderID());

        order.setProduct(mockedProd100, 10);
        order.setProduct(mockedProd300, 1);
        assertEquals(1100, order.getTotalCost(), 0.0001);

        String patternString = "\\*NOT FINALISED\\*\\nOrder details \\(id #0\\)\\nDate: [0-9]{4}-[0-9]{2}-[0-9]{2}\\nProducts:\\n\\tProduct name: Fake Product\\tQty: 10\\tUnit cost: \\$100\\.00\\tSubtotal: \\$1,000\\.00\\n\\tProduct name: Fake Product 2\\tQty: 1\\tUnit cost: \\$300\\.00\\tSubtotal: \\$300\\.00\\n\\tDiscount: -\\$200\\.00\\nTotal cost: \\$1,100\\.00\\n";
        Pattern pattern = Pattern.compile(patternString, Pattern.MULTILINE);
        assertTrue(pattern.matcher(order.longDesc()).matches());

        assertEquals("ID:0 $1,100.00", order.shortDesc());

        assertEquals("Your business account has been charged: $1,100.00\n" +
                "Please see your Brawndo© merchandising representative for itemised details.", order.generateInvoiceData());

        assertNotSame(order, order.copy());
    }

    @Test
    public void testOrderBusinessBulkSub() {
        setupLogin();
        when(mockedDB.getCustomerIDs(mockedToken)).thenReturn(Arrays.asList(1, 2, 3));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        doNothing().when(mockedDB).saveOrder(eq(mockedToken), captor.capture());

        int testOrderID = facade.createOrder(2, LocalDateTime.now(), true, true, 2, 10, 20, 10);
        facade.logout();

        verify(mockedDB).saveOrder(eq(mockedToken), any());
        Order order = captor.getValue();
        assertEquals(testOrderID, order.getOrderID());

        order.setProduct(mockedProd100, 10);
        order.setProduct(mockedProd300, 1);
        assertEquals(11000, order.getTotalCost(), 0.0001);

        String patternString = "\\*NOT FINALISED\\*\\nOrder details \\(id #0\\)\\nDate: [0-9]{4}-[0-9]{2}-[0-9]{2}\\nNumber of shipments: 10\\nProducts:\\n\\tProduct name: Fake Product\\tQty: 10\\tUnit cost: \\$100\\.00\\tSubtotal: \\$1,000\\.00\\n\\tProduct name: Fake Product 2\\tQty: 1\\tUnit cost: \\$300\\.00\\tSubtotal: \\$300\\.00\\n\\tDiscount: -\\$200\\.00\\nRecurring cost: \\$1,100\\.00\\nTotal cost: \\$11,000\\.00\\n";
        Pattern pattern = Pattern.compile(patternString, Pattern.MULTILINE);
        assertTrue(pattern.matcher(order.longDesc()).matches());

        assertEquals("ID:0 $1,100.00 per shipment, $1,100.00 total", order.shortDesc());

        assertEquals("Your business account will be charged: $1,100.00 each week, with a total overall cost of: $11,000.00\n" +
                "Please see your Brawndo© merchandising representative for itemised details.", order.generateInvoiceData());

        assertNotSame(order, order.copy());
    }

    @Test
    public void testOrderBusinessFlat() {
        setupLogin();
        when(mockedDB.getCustomerIDs(mockedToken)).thenReturn(Arrays.asList(1, 2, 3));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        doNothing().when(mockedDB).saveOrder(eq(mockedToken), captor.capture());

        int testOrderID = facade.createOrder(2, LocalDateTime.now(), true, false, 1, 100000, 50, 10);
        facade.logout();

        verify(mockedDB).saveOrder(eq(mockedToken), any());
        Order order = captor.getValue();
        assertEquals(testOrderID, order.getOrderID());

        order.setProduct(mockedProd100, 10);
        order.setProduct(mockedProd300, 1);

        String patternString = "\\*NOT FINALISED\\*\\nOrder details \\(id #0\\)\\nDate: [0-9]{4}-[0-9]{2}-[0-9]{2}\\nProducts:\\n\\tProduct name: Fake Product\\tQty: 10\\tUnit cost: \\$100\\.00\\tSubtotal: \\$1,000\\.00\\n\\tProduct name: Fake Product 2\\tQty: 1\\tUnit cost: \\$300\\.00\\tSubtotal: \\$300\\.00\\n\\tDiscount: -\\$650\\.00\\nTotal cost: \\$650\\.00\\n";
        Pattern pattern = Pattern.compile(patternString, Pattern.MULTILINE);
        assertTrue(pattern.matcher(order.longDesc()).matches());

        assertEquals(650, order.getTotalCost(), 0.0001);
        assertEquals("ID:0 $650.00", order.shortDesc());

        assertEquals("Your business account has been charged: $650.00\n" +
                "Please see your Brawndo© merchandising representative for itemised details.", order.generateInvoiceData());

        assertNotSame(order, order.copy());
    }

    @Test
    public void testOrderBusinessFlatSub() {
        setupLogin();
        when(mockedDB.getCustomerIDs(mockedToken)).thenReturn(Arrays.asList(1, 2, 3));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        doNothing().when(mockedDB).saveOrder(eq(mockedToken), captor.capture());

        int testOrderID = facade.createOrder(2, LocalDateTime.now(), true, true, 1, 10000, 50, 10);
        facade.logout();

        verify(mockedDB).saveOrder(eq(mockedToken), any());
        Order order = captor.getValue();
        assertEquals(testOrderID, order.getOrderID());

        order.setProduct(mockedProd100, 10);
        order.setProduct(mockedProd300, 1);

        String patternString = "\\*NOT FINALISED\\*\\nOrder details \\(id #0\\)\\nDate: [0-9]{4}-[0-9]{2}-[0-9]{2}\\nNumber of shipments: 10\\nProducts:\\n\\tProduct name: Fake Product\\tQty: 10\\tUnit cost: \\$100\\.00\\tSubtotal: \\$1,000\\.00\\n\\tProduct name: Fake Product 2\\tQty: 1\\tUnit cost: \\$300\\.00\\tSubtotal: \\$300\\.00\\n\\tDiscount: -\\$650\\.00\\nRecurring cost: \\$650\\.00\\nTotal cost: \\$6,500\\.00\\n";
        Pattern pattern = Pattern.compile(patternString, Pattern.MULTILINE);

        System.out.println(patternString);
        System.out.println(order.longDesc());

        assertTrue(pattern.matcher(order.longDesc()).matches());

        assertEquals(6500, order.getTotalCost(), 0.0001);

        assertEquals("ID:0 $650.00 per shipment, $650.00 total", order.shortDesc());

        assertEquals("Your business account will be charged: $650.00 each week, with a total overall cost of: $6,500.00\n" +
                "Please see your Brawndo© merchandising representative for itemised details.", order.generateInvoiceData());

        assertNotSame(order, order.copy());
    }

    @Test
    public void testOrderPersonalBulk() {
        setupLogin();
        when(mockedDB.getCustomerIDs(mockedToken)).thenReturn(Arrays.asList(1, 2, 3));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        doNothing().when(mockedDB).saveOrder(eq(mockedToken), captor.capture());

        int testOrderID = facade.createOrder(2, LocalDateTime.now(), false, false, 2, 10, 20, 10);
        facade.logout();

        verify(mockedDB).saveOrder(eq(mockedToken), any());
        Order order = captor.getValue();
        assertEquals(testOrderID, order.getOrderID());

        order.setProduct(mockedProd100, 10);
        order.setProduct(mockedProd300, 1);

        assertEquals(1100, order.getTotalCost(), 0.0001);

        String patternString = "\\*NOT FINALISED\\*\\nOrder details \\(id #0\\)\\nDate: [0-9]{4}-[0-9]{2}-[0-9]{2}\\nProducts:\\n\\tProduct name: Fake Product\\tQty: 10\\tUnit cost: \\$100\\.00\\tSubtotal: \\$1,000\\.00\\n\\tProduct name: Fake Product 2\\tQty: 1\\tUnit cost: \\$300\\.00\\tSubtotal: \\$300\\.00\\n\\tDiscount: -\\$200\\.00\\nTotal cost: \\$1,100\\.00\\n";
        Pattern pattern = Pattern.compile(patternString, Pattern.MULTILINE);
        assertTrue(pattern.matcher(order.longDesc()).matches());

        assertEquals("ID:0 $1,100.00", order.shortDesc());

        assertEquals("Thank you for your Brawndo© order!\n" +
                "Your order comes to: $1,100.00\n" +
                "Please see below for details:\n" +
                "\tProduct name: Fake Product\tQty: 10\tCost per unit: $100.00\tSubtotal: $1,000.00\n" +
                "\tProduct name: Fake Product 2\tQty: 1\tCost per unit: $300.00\tSubtotal: $300.00\n", order.generateInvoiceData());

        assertNotSame(order, order.copy());
    }

    @Test
    public void testOrderPersonalBulkSub() {
        setupLogin();
        when(mockedDB.getCustomerIDs(mockedToken)).thenReturn(Arrays.asList(1, 2, 3));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        doNothing().when(mockedDB).saveOrder(eq(mockedToken), captor.capture());

        int testOrderID = facade.createOrder(2, LocalDateTime.now(), false, true, 2, 10, 20, 10);
        facade.logout();

        verify(mockedDB).saveOrder(eq(mockedToken), any());
        Order order = captor.getValue();
        assertEquals(testOrderID, order.getOrderID());

        order.setProduct(mockedProd100, 10);
        order.setProduct(mockedProd300, 1);

        String patternString = "\\*NOT FINALISED\\*\\nOrder details \\(id #0\\)\\nDate: [0-9]{4}-[0-9]{2}-[0-9]{2}\\nNumber of shipments: 10\\nProducts:\\n\\tProduct name: Fake Product\\tQty: 10\\tUnit cost: \\$100\\.00\\tSubtotal: \\$1,000\\.00\\n\\tProduct name: Fake Product 2\\tQty: 1\\tUnit cost: \\$300\\.00\\tSubtotal: \\$300\\.00\\n\\tDiscount: -\\$200\\.00\\nRecurring cost: \\$1,100\\.00\\nTotal cost: \\$11,000\\.00\\n";
        Pattern pattern = Pattern.compile(patternString, Pattern.MULTILINE);
        assertTrue(pattern.matcher(order.longDesc()).matches());


        assertEquals(11000, order.getTotalCost(), 0.0001);

        assertEquals("ID:0 $1,100.00 per shipment, $1,100.00 total", order.shortDesc());

        assertEquals("Thank you for your Brawndo© order!\n" +
                "Your order comes to: $1,100.00 each week, with a total overall cost of: $11,000.00\n" +
                "Please see below for details:\n" +
                "\tProduct name: Fake Product\tQty: 10\tCost per unit: $100.00\tSubtotal: $1,000.00\n" +
                "\tProduct name: Fake Product 2\tQty: 1\tCost per unit: $300.00\tSubtotal: $300.00\n", order.generateInvoiceData());

        assertNotSame(order, order.copy());
    }

    @Test
    public void testOrderPersonalFlat() {
        setupLogin();
        when(mockedDB.getCustomerIDs(mockedToken)).thenReturn(Arrays.asList(1, 2, 3));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        doNothing().when(mockedDB).saveOrder(eq(mockedToken), captor.capture());

        int testOrderID = facade.createOrder(2, LocalDateTime.now(), false, false, 1, 10, 50, 10);
        facade.logout();

        verify(mockedDB).saveOrder(eq(mockedToken), any());
        Order order = captor.getValue();
        assertEquals(testOrderID, order.getOrderID());

        order.setProduct(mockedProd100, 10);
        order.setProduct(mockedProd300, 1);

        String patternString = "\\*NOT FINALISED\\*\\nOrder details \\(id #0\\)\\nDate: [0-9]{4}-[0-9]{2}-[0-9]{2}\\nProducts:\\n\\tProduct name: Fake Product\\tQty: 10\\tUnit cost: \\$100\\.00\\tSubtotal: \\$1,000\\.00\\n\\tProduct name: Fake Product 2\\tQty: 1\\tUnit cost: \\$300\\.00\\tSubtotal: \\$300\\.00\\n\\tDiscount: -\\$650\\.00\\nTotal cost: \\$650\\.00\\n";
        Pattern pattern = Pattern.compile(patternString, Pattern.MULTILINE);
        assertTrue(pattern.matcher(order.longDesc()).matches());

        assertEquals(650, order.getTotalCost(), 0.0001);
        assertEquals("ID:0 $650.00", order.shortDesc());

        assertEquals("Thank you for your Brawndo© order!\n" +
                "Your order comes to: $650.00\n" +
                "Please see below for details:\n" +
                "\tProduct name: Fake Product\tQty: 10\tCost per unit: $100.00\tSubtotal: $1,000.00\n" +
                "\tProduct name: Fake Product 2\tQty: 1\tCost per unit: $300.00\tSubtotal: $300.00\n", order.generateInvoiceData());

        assertNotSame(order, order.copy());
    }

    @Test
    public void testOrderPersonalFlatSub() {
        setupLogin();
        when(mockedDB.getCustomerIDs(mockedToken)).thenReturn(Arrays.asList(1, 2, 3));

        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        doNothing().when(mockedDB).saveOrder(eq(mockedToken), captor.capture());

        int testOrderID = facade.createOrder(2, LocalDateTime.now(), false, true, 1, 10, 50, 10);
        facade.logout();

        verify(mockedDB).saveOrder(eq(mockedToken), any());
        Order order = captor.getValue();
        assertEquals(testOrderID, order.getOrderID());

        order.setProduct(mockedProd100, 10);
        order.setProduct(mockedProd300, 1);

        String patternString = "\\*NOT FINALISED\\*\\nOrder details \\(id #0\\)\\nDate: [0-9]{4}-[0-9]{2}-[0-9]{2}\\nNumber of shipments: 10\\nProducts:\\n\\tProduct name: Fake Product\\tQty: 10\\tUnit cost: \\$100\\.00\\tSubtotal: \\$1,000\\.00\\n\\tProduct name: Fake Product 2\\tQty: 1\\tUnit cost: \\$300\\.00\\tSubtotal: \\$300\\.00\\n\\tDiscount: -\\$650\\.00\\nRecurring cost: \\$650\\.00\\nTotal cost: \\$6,500\\.00\\n";
        Pattern pattern = Pattern.compile(patternString, Pattern.MULTILINE);
        assertTrue(pattern.matcher(order.longDesc()).matches());

        assertEquals(6500, order.getTotalCost(), 0.0001);
        assertEquals("ID:0 $650.00 per shipment, $650.00 total", order.shortDesc());

        assertEquals("Thank you for your Brawndo© order!\n" +
                "Your order comes to: $650.00 each week, with a total overall cost of: $6,500.00\n" +
                "Please see below for details:\n" +
                "\tProduct name: Fake Product\tQty: 10\tCost per unit: $100.00\tSubtotal: $1,000.00\n" +
                "\tProduct name: Fake Product 2\tQty: 1\tCost per unit: $300.00\tSubtotal: $300.00\n", order.generateInvoiceData());

        assertNotSame(order, order.copy());
    }

    @Test
    public void getAllCustomerIDs() {
        boolean thrown = false;
        try {
            facade.getAllOrders();
        } catch (SecurityException ignored) {
            thrown = true;
        }

        assertTrue(thrown);

        setupLogin();
        try (MockedStatic<AuthModule> authModuleMock = mockStatic(AuthModule.class)) {
        when(mockedDB.getCustomerIDs(mockedToken)).thenReturn(Arrays.asList(1, 4, 7));

        List<Integer> result = facade.getAllCustomerIDs();
        result.sort(Comparator.naturalOrder());

        assertEquals(Arrays.asList(1, 4, 7), result);
        verify(mockedDB).getCustomerIDs(mockedToken);
    }
    }

    //@Test
    public void getCustomer() {
        boolean thrown = false;
        try {
            facade.getCustomer(1);
        } catch (SecurityException ignored) {
            thrown = true;
        }

        assertTrue(thrown);

        setupLogin();

        when(mockedDB.getCustomerField(mockedToken, 1, "fName")).thenReturn("First");
        when(mockedDB.getCustomerField(mockedToken, 1, "lName")).thenReturn("Last");
        when(mockedDB.getCustomerField(mockedToken, 1, "phoneNumber")).thenReturn("12345");
        //when(mockedDB.getCustomerField(mockedToken, 1, "emailAddress")).thenReturn("email@provider.com");
        when(mockedDB.getCustomerField(mockedToken, 1, "address")).thenReturn("123 Fake St");
        when(mockedDB.getCustomerField(mockedToken, 1, "suburb")).thenReturn("Springfield");
        when(mockedDB.getCustomerField(mockedToken, 1, "state")).thenReturn("NSW");
        when(mockedDB.getCustomerField(mockedToken, 1, "postCode")).thenReturn("2830");
        //when(mockedDB.getCustomerField(mockedToken, 1, "merchandiser")).thenReturn("Frank");
        when(mockedDB.getCustomerField(mockedToken, 1, "businessName")).thenReturn("Qwik-E-Mart");
        when(mockedDB.getCustomerField(mockedToken, 1, "pigeonCoopID")).thenReturn("117");

        Customer result = facade.getCustomer(1);

        assertEquals("First", result.getfName());
        assertEquals("Last", result.getlName());
        assertEquals("12345", result.getPhoneNumber());
        assertNull(result.getEmailAddress());
        assertEquals("123 Fake St", result.getAddress());
        assertEquals("Springfield", result.getSuburb());
        assertEquals("NSW", result.getState());
        assertEquals("2830", result.getPostCode());
        assertNull(result.getMerchandiser());
        assertEquals("Qwik-E-Mart", result.getBusinessName());
        assertEquals("117", result.getPigeonCoopID());

        when(mockedDB.getCustomerField(mockedToken, 1, "emailAddress")).thenReturn("email@provider.com");
        when(mockedDB.getCustomerField(mockedToken, 1, "merchandiser")).thenReturn("Frank");

        result = facade.getCustomer(1);
        assertEquals("email@provider.com", result.getEmailAddress());
        assertEquals("Frank", result.getMerchandiser());
    }

    @Test
    public void removeOrder() {
        boolean thrown = false;
        try {
            facade.removeOrder(1);
        } catch (SecurityException ignored) {
            thrown = true;
        }

        assertTrue(thrown);

        setupLogin();

        when(mockedDB.removeOrder(mockedToken, 1)).thenReturn(true);
        when(mockedDB.removeOrder(mockedToken, 2)).thenReturn(false);

        boolean result = facade.removeOrder(1);

        assertTrue(result);
        verify(mockedDB).removeOrder(mockedToken, 1);
        verifyNoMoreInteractions(mockedDB);

        result = facade.removeOrder(2);

        assertFalse(result);
        verify(mockedDB).removeOrder(mockedToken, 2);
        verifyNoMoreInteractions(mockedDB);
    }

    @Test
    public void getAllProducts() {
        boolean thrown = false;
        try {
            facade.getAllProducts();
        } catch (SecurityException ignored) {
            thrown = true;
        }

        assertTrue(thrown);

        setupLogin();

        mockStatic(ProductDatabase.class);

        Collection<Product> response = Collections.singletonList(new ProductImpl("test product", 1.0, null, null, null, null, null));

        when(ProductDatabase.getTestProducts()).thenReturn(response);

        assertEquals(response, facade.getAllProducts());

        //verifyStatic(ProductDatabase.class);
        ProductDatabase.getTestProducts();
    }

    //@Test
    public void finaliseOrder() {
        boolean thrown = false;
        try {
            facade.finaliseOrder(1, Arrays.asList("first", "second"));
        } catch (SecurityException ignored) {
            thrown = true;
        }

        assertTrue(thrown);

        setupLogin();

        when(mockedDB.getCustomerField(mockedToken, 1, "fName")).thenReturn("First");
        when(mockedDB.getCustomerField(mockedToken, 1, "lName")).thenReturn("Last");
        when(mockedDB.getCustomerField(mockedToken, 1, "phoneNumber")).thenReturn("12345");

        Order mockedOrder = mock(Order.class);
        when(mockedOrder.generateInvoiceData()).thenReturn("Invoice data for mocked order");
        when(mockedOrder.getCustomer()).thenReturn(1);
        when(mockedDB.getOrder(mockedToken, 1)).thenReturn(mockedOrder);

        PrintStream resetOut = System.out;
        ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOut));

        facade.finaliseOrder(1, Arrays.asList(
                "Carrier Pigeon",
                "Email",
                "Mail",
                "Merchandiser",
                "Phone call"
        ));

        String standardOutput = capturedOut.toString();
        System.setOut(resetOut);

        assertThat(standardOutput, containsString("Invoice data for mocked order"));
        assertThat(standardOutput, containsString("Now robodialling First Last at 12345!"));

        /*
         Consider the following default contact order a standard requirement

        if (contactPriorityAsMethods.size() == 0) { // needs setting to default
            contactPriorityAsMethods = Arrays.asList(
                    ContactMethod.MERCHANDISER,
                    ContactMethod.EMAIL,
                    ContactMethod.CARRIER_PIGEON,
                    ContactMethod.MAIL,
                    ContactMethod.PHONECALL
            );
        }

        */
    }

    //@Test
    public void logout() {
        mockedToken = mock(AuthToken.class);
        when(AuthModule.login("username", "password")).thenReturn(mockedToken);
        when(AuthModule.authenticate(mockedToken)).thenReturn(true); // This works for cross-dependencies like db or contact

        facade.login("username", "password");

        facade.logout();

        //verifyStatic(AuthModule.class);
        AuthModule.logout(mockedToken);

        boolean thrown = false;
        try {
            facade.getAllOrders();
        } catch (SecurityException ignored) {
            thrown = true;
        }

        assertTrue(thrown);
    }

    @Test
    public void getOrderTotalCost() {
        boolean thrown = false;
        try {
            facade.getOrderTotalCost(1);
        } catch (SecurityException ignored) {
            thrown = true;
        }

        assertTrue(thrown);

        setupLogin();

        Order mockedOrder = mock(Order.class);
        when(mockedOrder.getTotalCost()).thenReturn(1234.56);

        when(mockedDB.getOrder(mockedToken, 1)).thenReturn(mockedOrder);

        assertEquals(1234.56, facade.getOrderTotalCost(1), 0.0001);
        verify(mockedOrder).getTotalCost();
    }

    @Test
    public void orderLineSet() {
        boolean thrown = false;
        try {
            facade.orderLineSet(1, null, 1);
        } catch (SecurityException ignored) {
            thrown = true;
        }

        assertTrue(thrown);

        setupLogin();

        Product mockedProduct = mock(Product.class);
        Order mockedOrder = mock(Order.class);
        when(mockedDB.getOrder(mockedToken, 1)).thenReturn(mockedOrder);

        facade.orderLineSet(1, mockedProduct, 17);
        facade.logout();

        verify(mockedOrder).setProduct(mockedProduct, 17);
    }

    @Test
    public void getOrderLongDesc() {
        boolean thrown = false;
        try {
            facade.getOrderLongDesc(1);
        } catch (SecurityException ignored) {
            thrown = true;
        }

        assertTrue(thrown);

        setupLogin();

        Order mockedOrder = mock(Order.class);
        when(mockedOrder.longDesc()).thenReturn("a long desc");

        when(mockedDB.getOrder(mockedToken, 1)).thenReturn(mockedOrder);

        assertEquals("a long desc", facade.getOrderLongDesc(1));
        verify(mockedOrder).longDesc();
    }

    @Test
    public void getOrderShortDesc() {
        boolean thrown = false;
        try {
            facade.getOrderShortDesc(1);
        } catch (SecurityException ignored) {
            thrown = true;
        }

        assertTrue(thrown);

        setupLogin();

        Order mockedOrder = mock(Order.class);
        when(mockedOrder.shortDesc()).thenReturn("a short desc");

        when(mockedDB.getOrder(mockedToken, 1)).thenReturn(mockedOrder);

        assertEquals("a short desc", facade.getOrderShortDesc(1));
        verify(mockedOrder).shortDesc();
    }

    @Test
    public void getKnownContactMethods() {
        boolean thrown = false;
        try {
            facade.getKnownContactMethods();
        } catch (SecurityException ignored) {
            thrown = true;
        }

        assertTrue(thrown);

        setupLogin();

        List<String> expected = Arrays.asList(
                "Carrier Pigeon",
                "Email",
                "Mail",
                "Merchandiser",
                "Phone call",
                "SMS"
        );

        expected.sort(Comparator.naturalOrder());

        List<String> response = facade.getKnownContactMethods();

        response.sort(Comparator.naturalOrder());

        assertEquals(expected, response);
    }
}
