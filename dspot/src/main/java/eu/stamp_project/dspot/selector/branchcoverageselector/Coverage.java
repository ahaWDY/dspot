package eu.stamp_project.dspot.selector.branchcoverageselector;

import com.atlassian.clover.registry.entities.MethodSignature;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Coverage {
    public void clear() {
        this.testClassCoverageMap.clear();
    }

    public int size() {
        return this.testClassCoverageMap.size();
    }

    private final Map<String, TestClassCoverage> testClassCoverageMap;


    public Coverage() {
        this.testClassCoverageMap = new ConcurrentHashMap<>();
    }

    public synchronized void addBranchCoverage(String testClassName, String testMethodName, String className, String methodName, Region region, int trueHitCount, int falseHitCount) {
        if (!this.testClassCoverageMap.containsKey(testClassName)) {
            this.testClassCoverageMap.put(testClassName, new TestClassCoverage(testClassName));
        }
        this.testClassCoverageMap.get(testClassName).addBranchCoverage(testMethodName, className, methodName, region, trueHitCount, falseHitCount);
    }

    public synchronized void addLineCoverage(String testClassName, String testMethodName, String className, String methodName, int line, int hitCount) {
        if (!this.testClassCoverageMap.containsKey(testClassName)) {
            this.testClassCoverageMap.put(testClassName, new TestClassCoverage(testClassName));
        }
        this.testClassCoverageMap.get(testClassName).addLineCoverage(testMethodName, className, methodName, line, hitCount);
    }

    public Set<String> getTestClasses() {
        return this.testClassCoverageMap.keySet();
    }


    public Map<String, MethodCoverage> getCoverageForTestClassTestMethodAndClassName(String testClassName, String testMethodName, String className) {
        return this.testClassCoverageMap.get(testClassName).getCoverageForTestMethodAndClassNames(testMethodName, className);
    }

    public List<BranchCoverage> getBranchCoverageForTestClassAndClassNameMethodName(String testClassName, String className, String methodName){
        List<BranchCoverage> result = new ArrayList<>();
        TestClassCoverage testClassCoverage = testClassCoverageMap.get(testClassName);
        for(Map.Entry<String, TestMethodCoverage> entry: testClassCoverage.getTestMethodsCoverage().entrySet()){
            List<BranchCoverage> tmp = entry.getValue().getBranchCoverageForClassAndMethod(className, methodName);
            if(tmp!=null) {
                result.addAll(tmp);
            }
        }
        return result;
    }

    public List<LineCoverage> getLineCoverageForTestClassAndClassNameMethodName(String testClassName, String className, String methodName){
        List<LineCoverage> result = new ArrayList<>();
        TestClassCoverage testClassCoverage = testClassCoverageMap.get(testClassName);
        for(Map.Entry<String, TestMethodCoverage> entry: testClassCoverage.getTestMethodsCoverage().entrySet()){
            List<LineCoverage> tmp = entry.getValue().getLineCoverageForClassAndMethod(className, methodName);
            if(tmp!=null) {
                result.addAll(tmp);
            }
        }
        return result;
    }

    public List<BranchCoverage> getBranchCoverageForTestClassTestMethodAndClassNameMethodName(String testClassName, String testMethodName, String className, String methodName){
        if(!testClassCoverageMap.containsKey(testClassName)){
            return null;
        }
        TestMethodCoverage testMethodCoverage =  this.testClassCoverageMap.get(testClassName).getTestMethodCoverage(testMethodName);
        if(testMethodCoverage!=null) {
            return testMethodCoverage.getBranchCoverageForClassAndMethod(className, methodName);
        }
        return null;
    }


    public List<LineCoverage> getLineCoverageForTestClassTestMethodAndClassNameMethodName(String testClassName, String testMethodName, String className, String methodName){
        if(!testClassCoverageMap.containsKey(testClassName)){
            return null;
        }
        TestMethodCoverage testMethodCoverage =  this.testClassCoverageMap.get(testClassName).getTestMethodCoverage(testMethodName);
        if(testMethodCoverage!=null) {
            return testMethodCoverage.getLineCoverageForClassAndMethod(className, methodName);
        }
        return null;
    }

    public List<LineCoverage> getLineCoverageForTestClassTestMethodAndClassName(String testClassName, String testMethodName, String className){
        Map<String, MethodCoverage> methodCoverageMap = getCoverageForTestClassTestMethodAndClassName(testClassName, testMethodName, className);
        List<LineCoverage> result = new ArrayList<>();
        if(methodCoverageMap==null){
            return result;
        }
        for(MethodCoverage methodCoverage: methodCoverageMap.values()){
            result.addAll(methodCoverage.getLineCoverageList());
        }
        return result;
    }

    public List<BranchCoverage> getBranchCoverageForTestClassTestMethodAndClassName(String testClassName, String testMethodName, String className){
        Map<String, MethodCoverage> methodCoverageMap = getCoverageForTestClassTestMethodAndClassName(testClassName, testMethodName, className);
        List<BranchCoverage> result = new ArrayList<>();
        if(methodCoverageMap==null){
            return result;
        }
        for(MethodCoverage methodCoverage: methodCoverageMap.values()){
            result.addAll(methodCoverage.getBranchCoverageList());
        }
        return result;
    }


    @Override
    public String toString() {
        return "Coverage{" +
                "testClassCoverage=" + testClassCoverageMap.toString() +
                '}';
    }
}
