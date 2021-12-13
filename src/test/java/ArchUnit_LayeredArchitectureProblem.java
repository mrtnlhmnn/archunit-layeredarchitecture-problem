import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ArchUnit_LayeredArchitectureProblem {

    String PKG = "mycomponent";
    JavaClasses myClasses = new ClassFileImporter().importPackages(PKG);

    @Test
    void test_layers() {
        String layerA = "LayerA";
        String layerB = "LayerB";
        String layerC = "LayerC";

        Architectures.LayeredArchitecture layeredArchitecture = Architectures.layeredArchitecture()
                .layer(layerA).definedBy(PKG+".layerA..")
                .layer(layerB).definedBy(PKG+".layerB..")
                .layer(layerC).definedBy(PKG+".layerC..")
                // ignore any dependencies to java.. Not sure if this ignore could be avoided
                .ignoreDependency(isMyClass, isJavaClass)
                // no access to A
                .whereLayer(layerA).mayNotBeAccessedByAnyLayer()
                // A --can access--> B
                .whereLayer(layerA).mayOnlyAccessLayers(layerB)
                .whereLayer(layerB).mayOnlyBeAccessedByLayers(layerA)
                // B --can access--> C
                .whereLayer(layerC).mayOnlyBeAccessedByLayers(layerB)
             // .whereLayer(layerB).mayOnlyAccessLayers(layerC)  // <== problem here, comment in to show
                ;
        layeredArchitecture.check(myClasses);

/*
   Here is our problem:
   As soon as the last "whereLayer" statement in line 34 is commented in, the test fails because of
       Field <mycomponent.layerA.A.b> has type <mycomponent.layerB.B> in (A.java:0)

   But why does an allowed access from B->C bring up an error for an access from A->B
   (which is even explicitely allowed)?
 */
    }

    DescribedPredicate<JavaClass> isMyClass = new DescribedPredicate<>("is in mycomponent") {
        @Override
        public boolean apply(JavaClass input) {
            return input.getPackageName().startsWith(PKG);
        }
    };

    DescribedPredicate<JavaClass> isJavaClass = new DescribedPredicate<>("is Java class") {
        @Override
        public boolean apply(JavaClass input) {
            return input.getPackageName().startsWith("java");
        }
    };
}
