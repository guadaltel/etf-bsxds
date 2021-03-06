import module namespace etfxdb = "http://interactive_instruments.de/etf/etfxdb";

declare default element namespace "http://www.interactive-instruments.de/etf/2.0";
declare namespace etf = "http://www.interactive-instruments.de/etf/2.0";
declare namespace xs = 'http://www.w3.org/2001/XMLSchema';

declare variable $function external;

declare variable $qids external := "";

declare variable $offset external := 0;
declare variable $limit external := 0;
declare variable $levelOfDetail external := 'SIMPLE';
declare variable $fields external := '*';

declare function local:get-testruns($offset as xs:integer, $limit as xs:integer) {
        <DsResultSet
            xmlns="http://www.interactive-instruments.de/etf/2.0"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xmlns:etf="http://www.interactive-instruments.de/etf/2.0"
            xsi:schemaLocation="http://www.interactive-instruments.de/etf/2.0 http://resources.etf-validator.net/schema/v2/model/resultSet.xsd">
            <testRuns>
                {etfxdb:get-all(db:open('etf-ds')/etf:TestRun, $offset, $limit, $fields)}
            </testRuns>
        </DsResultSet>
};

declare function local:get-testrun($ids as xs:string*) {
    let $testRunDb := db:open('etf-ds')/etf:TestRun
    let $testObjectTypesDb := db:open('etf-ds')/etf:TestObjectType
    let $testObjectsDb := db:open('etf-ds')/etf:TestObject
    let $executableTestSuiteDb := db:open('etf-ds')/etf:ExecutableTestSuite
    let $translationTemplateBundleDb := db:open('etf-ds')/etf:TranslationTemplateBundle
    let $testTaskResultsDb := db:open('etf-ds')/etf:TestTaskResult

    let $testRun := $testRunDb[@id = $ids]
    let $executableTestSuite := etfxdb:get-executableTestSuites($executableTestSuiteDb, $levelOfDetail, $testRun/etf:testTasks/etf:TestTask)
    let $testObjects := etfxdb:get-testObjects($testObjectsDb, $levelOfDetail, $testRun/etf:testTasks/etf:TestTask)

    return
        <DsResultSet
        xmlns="http://www.interactive-instruments.de/etf/2.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:etf="http://www.interactive-instruments.de/etf/2.0"
        xsi:schemaLocation="http://www.interactive-instruments.de/etf/2.0 http://resources.etf-validator.net/schema/v2/model/resultSet.xsd">
            <testItemTypes>
                {etfxdb:get-testItemTypes($testObjectsDb, $levelOfDetail,
                        $executableTestSuite/etf:testModules[1]/etf:TestModule/etf:testCases[1]/etf:TestCase/etf:testSteps[1]/etf:TestStep/(self::node() |
                                testAssertions[1]/etf:TestAssertion) )}
            </testItemTypes>
            <executableTestSuites>
                {$executableTestSuite}
            </executableTestSuites>
            <testObjects>
                {$testObjects}
            </testObjects>
            <testObjectTypes>
                {etfxdb:get-testObjectTypes($testObjectTypesDb, $levelOfDetail, ($executableTestSuite/etf:supportedTestObjectTypes,$executableTestSuite/etf:consumableResultObjectTypes, $testObjects/etf:testObjectTypes))}
            </testObjectTypes>
            <translationTemplateBundles>
                {etfxdb:get-translationTemplateBundles($translationTemplateBundleDb, $levelOfDetail, $executableTestSuite)}
            </translationTemplateBundles>
            <testRuns>
                {$testRun}
            </testRuns>
            <testTaskResults>
               {etfxdb:get-testTaskResults($testTaskResultsDb, $levelOfDetail, $testRun/etf:testTasks/etf:TestTask)}
            </testTaskResults>
        </DsResultSet>
};

if ($function = 'byId')
then
    local:get-testrun($qids)
else
    local:get-testruns($offset, $limit)
