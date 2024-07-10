# SPFEA/ERP System Refactoring Project

## Background

THE FOLLOWING IS THE TASK DESCRIPTION FOR THE PROJECT I COMPLETED. A CODEBASE WAS PROVIDED AND I WAS TASKED WITH IMPROVING IT THOUGH THE USE OF DESIGN PATTERNS. I ATTAINED FULL MARKS FOR THIS PROJECT

It’s been a few decades since your last work with Brawndo (you have been in cryosleep), and the SPFEA/ERP system you helped them with has not been treated well. Many consultants have come and gone on the system, and there have been several “rush” jobs that focused on shipping code that worked and not on code that worked (or was designed) well.

The end result is a system that works… sort of. All of the functional tests pass, and the system mostly delivers what the customers want. However, it is very slow in some places (causing non-functional tests to fail), and any time Brawndo asks someone to add a new feature or fix the lag issues they run away in terror.

Brawndo have now asked you to come in and fix one of their key components, but since money is rather tight they have placed restrictions on your work:

- Several ‘module borders’ in the system exist – you may not change anything beyond the module border. These modules are pretend abstractions of legacy software the organisation has lost the source code for and does not have the time or money to redevelop.
- Brawndo has the existing API for these modules and has given you a 'pretend' version of each module for each to test with.
- Your solution will be run through a test suite (provided to you as well) to ensure the module interactivity remains intact at the end, and to ensure your delivered code still responds in precisely the same functional way as before.
- Thankfully, the module you will be redeveloping was behind a façade just like your original design – so besides a few public interfaces you can change anything you like about its internal workings without worrying about the view not being able to handle the changes.

You should be very careful when refactoring this code. The code currently passes function-based testing and should continue to do so the entire time - no breaking changes!

## Work Required
You must dig into and correct the design and implementation of the SPFEAModule package along with all involved classes. Brawndo has observed the following key issues they would like you to investigate and solve:

### RAM Issue
The system uses a LOT of RAM. Analysis has indicated this is due to the Product class, which stores a lot of data. Brawndo would like you to solve this RAM issue somehow, without breaking the existing use of the Product interface. ProductImpl has been included in your module scope to assist with this, but ProductDatabase is a fake façade on a remote database that you cannot change.

### Too Many Orders
There are several types of Orders. The current solution for these orders is to create a new class for each type (based on discounting method, whether the order is a personal or business order, and whether the order is a one-off or subscription). The full system has 66 * 2 * 2 of these classes (264 order classes!), with 8 of these (2*2*2) provided to you as an example – Brawndo would like you to find a way to reduce this class load without breaking the existing Order interface.

### Bulky Contact Method
The current method of handling customer contact methods is quite bulky (e.g., ContactHandler.sendInvoice() is hard to maintain and is getting quite long) – Brawndo would like you to streamline this somehow.

### System Lag
Any time Customers are loaded from the database, the system loads for a long time – even if only 1 field of 1 customer is needed (like id). The database issues themselves have been deemed too expensive to fix, but perhaps you can partially mitigate this with the software somehow?

### Hard to Compare Products
Because the Product object captures data without any consistent primary key, and because people have duplicated object names and versions, any time products need to be compared for equality we have to check many fields (in code). Brawndo would like you to make this process simpler.

### Slow Order Creation
The Order creation process involves a lot of slow database operations. Brawndo would like you to simplify this process (especially the database lag) without breaking the Order interface. The users are particularly annoyed that they have to experience system load between every change made to the order (before the order is finalized).

As well as these, they would like you to clean up the code (only within `au.edu.sydney.brawndo.erp.spfea` as described in the scope section) and document (java docs and inline comments for more complex blocks) where you believe it is necessary (for both the existing code and your changes). We recommend the Google Style Guide, if you choose another guide you will need to note that in the "Notes About the Submission" section of the README.

## Detailed Allowed Scope
The ONLY packages you are allowed to modify is `au.edu.sydney.brawndo.erp.spfea` contents (all classes in `spfea.ordering` and `spfea.products` (except for `ProductDatabase`) and all other existing classes in `spfea`, `SPFEAFacade` has limitations as can be seen below).

- You may not modify `spfea.products.ProductDatabase` (it's not the real database and modifying this won't help Brawndo).
- You may not replace the uses of `auth`, `contact`, `database`, `ordering`, or `view` packages - the test suite will enforce this.
- You may add, remove, merge, change, etc any of the in-scope classes, so long as the public API of the `spfea` package remains the same - i.e., `SPFEAFacade` must still exist in the same way so view can call its public methods, so the method signature needs to remain the same. You can, however, add private fields and methods to the facade as needed and you can modify the code in the public-facing methods to facilitate your improved design.
- You may not change the provided test suite, you can write additional tests to ensure the quality of your work, clearly separate any tests from the provided suite (i.e., different module and/or test files).
- You may not change the provided `build.gradle` file.

---

### RAM Issue
#### Solution: FlyWeight Design Pattern 
- Client : ProductImpl.java (spfea.products.ProductImpl)
- FlyWeight : Flyweight.java (spfea.products.Flyweight)
- ConcreteFlyWeight : DataFlyweight.java (spfea.products.DataFlyweight)
- FlyWeight Factory: FlyWeightFactory.java (spfea.products.FlyWeightFactory)

#### Used In Conjunction With Singleton Design Pattern (Alternative Pattern)
- Singleton : FlyWeightFactory.java (spfea.products.FlyWeightFactory)
- Ensured One single Instance of FlyWeightFactory was created  to ensure providing the ability to access the Factory from productImpl and adhere to design principles.


---
# Implemented Solutions

### Too Many Orders
#### Solution: Strategy Design Pattern 

DiscountStrategy - (spfea.ordering.strategies.discountstrats):

- Strategy : DiscountStrategy.java
- ConcreteStrategy : FlatRateDiscountStrat.java
- ConcreteStrategy : BulkDiscountStrat.java
- Context : OrderImpl.java (spfea.ordering.OrderImpl)
- Context : SubscriptionOrderImpl.java (ordering.SupscriptionOrderImpl)
- Client : SPFEAFacade.java (spfea.SPFEAFacade)

InvoiceStrategy - (spfea.ordering.strategies.invoicestrats):

- Strategy : CustomerInvoiceStrategy.java
- ConcreteStrategy : BusinessInvoiceStrat.java
- ConcreteStrategy : PersonalInvoiceStrat.java
- Context : OrderImpl.java (spfea.ordering.OrderImpl)
- Context : SubscriptionOrderImpl.java (ordering.SupscriptionOrderImpl)
- Client : SPFEAFacade.java (spfea.SPFEAFacade)

---


### Bulky Contact Method
#### Solution: Chain Of Responsibility (spfea.contacthandlerchain)
- Client : ContactHandler.java (spfea.ContactHandler)
- Handler : ContactMethodHandler.java 
- ConcreteHandler : CarrierPigeonHandler.java 
- ConcreteHandler : EmailHandler.java 
- ConcreteHandler : MailHandler.java 
- ConcreteHandler : MerchandiserHandler.java 
- ConcreteHandler : PhoneCallHandler.java 
- ConcreteHandler : SMSHandler.java 


---

### System Lag
#### Solution: Lazy Loading (Virtual Proxy):
- Client : SPFEAFacade.java 
- ExpensiveObject : Customer.java (ordering.Customer)
- ExpensiveObjectImpl : CustomerImpl.java (spfea.CustomerImpl)
- ExpensiveObjectProxy : CustomerProxy.java (spfea.CustomerProxy)


---

### Hard to Compare Products
#### Solution: Value Object
- ValueObject : ProductImpl.java (spfea.products.ProductImpl)

Equals and hashcode methods are overridden in the class for equality comparisons 


---


### Slow Order Creation
#### Solution: Unit Of Work
- Unit Of Work : OrderUoW.java
- Client : SPFEAFacade.java 

---
