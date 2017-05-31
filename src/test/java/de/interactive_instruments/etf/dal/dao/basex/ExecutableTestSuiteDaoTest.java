/**
 * Copyright 2010-2017 interactive instruments GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.interactive_instruments.etf.dal.dao.basex;

import static de.interactive_instruments.etf.dal.dao.basex.BsxTestUtils.*;
import static de.interactive_instruments.etf.test.TestDtos.*;
import static junit.framework.TestCase.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.*;
import org.junit.runners.MethodSorters;

import de.interactive_instruments.IFile;
import de.interactive_instruments.TimeUtils;
import de.interactive_instruments.etf.dal.dao.*;
import de.interactive_instruments.etf.dal.dto.IncompleteDtoException;
import de.interactive_instruments.etf.dal.dto.test.ExecutableTestSuiteDto;
import de.interactive_instruments.etf.dal.dto.test.TestItemTypeDto;
import de.interactive_instruments.etf.model.DefaultEidMap;
import de.interactive_instruments.etf.model.EID;
import de.interactive_instruments.etf.model.EidFactory;
import de.interactive_instruments.etf.model.EidMap;
import de.interactive_instruments.exceptions.InitializationException;
import de.interactive_instruments.exceptions.InvalidStateTransitionException;
import de.interactive_instruments.exceptions.ObjectWithIdNotFoundException;
import de.interactive_instruments.exceptions.StorageException;
import de.interactive_instruments.exceptions.config.ConfigurationException;

/**
 * @author Jon Herrmann ( herrmann aT interactive-instruments doT de )
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ExecutableTestSuiteDaoTest {

	private static WriteDao<ExecutableTestSuiteDto> writeDao;

	private final static Filter ALL = new Filter() {
		@Override
		public int offset() {
			return 0;
		}

		@Override
		public int limit() {
			return 2000;
		}
	};

	@BeforeClass
	public static void setUp() throws ConfigurationException, InvalidStateTransitionException, InitializationException,
			StorageException, ObjectWithIdNotFoundException, IOException {
		BsxTestUtils.ensureInitialization();
		writeDao = ((WriteDao) DATA_STORAGE.getDao(ExecutableTestSuiteDto.class));

		TestObjectDaoTest.setUp();

		BsxTestUtils.forceDeleteAndAdd(TTB_DTO_1);

		BsxTestUtils.forceDeleteAndAdd(TOT_DTO_3);

		BsxTestUtils.forceDeleteAndAdd(COMP_DTO_1);

		BsxTestUtils.forceDeleteAndAdd(ASSERTION_TYPE_1);

		BsxTestUtils.forceDeleteAndAdd(TESTSTEP_TYPE_2);

		final EidMap<TestItemTypeDto> testItemTypes = new DefaultEidMap<TestItemTypeDto>() {
			{
				{
					final TestItemTypeDto testItemTypeDto = new TestItemTypeDto();
					testItemTypeDto.setLabel("TestStep.Type.1");
					testItemTypeDto.setId(EidFactory.getDefault().createAndPreserveStr("f483e8e8-06b9-4900-ab36-adad0d7f22f0"));
					testItemTypeDto.setDescription("TODO description");
					testItemTypeDto.setReference(
							"https://github.com/interactive-instruments/etf-bsxtd/wiki/Test-Step-Types#teststeptype1");
					put(testItemTypeDto.getId(), testItemTypeDto);
				}
				{
					final TestItemTypeDto testItemTypeDto = new TestItemTypeDto();
					testItemTypeDto.setLabel("TestAssertion.Type.1");
					testItemTypeDto.setId(EidFactory.getDefault().createAndPreserveStr("f0edc596-49d2-48d6-a1a1-1ac581dcde0a"));
					testItemTypeDto.setDescription("TODO description");
					testItemTypeDto.setReference(
							"https://github.com/interactive-instruments/etf-bsxtd/wiki/Test-Assertion-Types#test-assertion-type-1");
					put(testItemTypeDto.getId(), testItemTypeDto);
				}
				{
					final TestItemTypeDto testItemTypeDto = new TestItemTypeDto();
					testItemTypeDto.setLabel("TestAssertion.Type.2");
					testItemTypeDto.setId(EidFactory.getDefault().createAndPreserveStr("b48eeaa3-6a74-414a-879c-1dc708017e11"));
					testItemTypeDto.setDescription("TODO description");
					testItemTypeDto.setReference(
							"https://github.com/interactive-instruments/etf-bsxtd/wiki/Test-Assertion-Types#test-assertion-type-2");
					put(testItemTypeDto.getId(), testItemTypeDto);
				}
				{
					final TestItemTypeDto testItemTypeDto = new TestItemTypeDto();
					testItemTypeDto.setLabel("TestAssertion.Type.3");
					testItemTypeDto.setId(EidFactory.getDefault().createAndPreserveStr("f0edc596-49d2-48d6-a1a1-1ac581dcde0a"));
					testItemTypeDto.setDescription("TODO description");
					testItemTypeDto.setReference(
							"https://github.com/interactive-instruments/etf-bsxtd/wiki/Test-Assertion-Types#test-assertion-type-3");
					put(testItemTypeDto.getId(), testItemTypeDto);
				}
				{
					final TestItemTypeDto testItemTypeDto = new TestItemTypeDto();
					testItemTypeDto.setLabel("TestAssertion.Type.4");
					testItemTypeDto.setId(EidFactory.getDefault().createAndPreserveStr("92f22a19-2ec2-43f0-8971-c2da3eaafcd2"));
					testItemTypeDto.setDescription("TODO description");
					testItemTypeDto.setReference(
							"https://github.com/interactive-instruments/etf-bsxtd/wiki/Test-Assertion-Types#test-assertion-type-4");
					put(testItemTypeDto.getId(), testItemTypeDto);
				}

			}
		};
		testItemTypes.values().forEach(type -> {
			try {
				BsxTestUtils.forceDeleteAndAdd(type);
			} catch (StorageException e) {
				e.printStackTrace();
			} catch (ObjectWithIdNotFoundException e) {
				e.printStackTrace();
			}
		});

	}

	@AfterClass
	public static void tearDown()
			throws ConfigurationException, InvalidStateTransitionException, InitializationException, StorageException {
		TestObjectDaoTest.tearDown();
	}

	@Before
	public void clean() throws StorageException {
		BsxTestUtils.forceDelete(ETS_DTO_1);
		BsxTestUtils.forceDelete(ETS_DTO_2);
		BsxTestUtils.forceDelete(ETS_DTO_3);
	}

	@Test(timeout = 15000)
	public void test_2_0_add_and_get() throws StorageException, ObjectWithIdNotFoundException {
		assertTrue(!writeDao.exists(ETS_DTO_3.getId()) || writeDao.isDisabled(ETS_DTO_3.getId()));
		writeDao.add(ETS_DTO_1);
		assertTrue(writeDao.exists(ETS_DTO_1.getId()));

		final PreparedDto<ExecutableTestSuiteDto> preparedDto = writeDao.getById(ETS_DTO_1.getId());
		// Check internal ID
		assertEquals(ETS_DTO_1.getId(), preparedDto.getDtoId());
		final ExecutableTestSuiteDto dto = preparedDto.getDto();
		assertNotNull(dto);
		assertEquals(ETS_DTO_1.getId(), dto.getId());
		assertEquals(ETS_DTO_1.toString(), dto.toString());
		assertNotNull(dto.getParameters());
		assertEquals("Parameter.1.key", dto.getParameters().getParameter("Parameter.1.key").getName());
		assertEquals("Parameter.1.value", dto.getParameters().getParameter("Parameter.1.key").getDefaultValue());

		assertEquals("Parameter.2.key", dto.getParameters().getParameter("Parameter.2.key").getName());
		assertEquals("Parameter.2.value", dto.getParameters().getParameter("Parameter.2.key").getDefaultValue());
	}

	@Test(timeout = 15000)
	public void test_2_1_get_disabled() throws StorageException, ObjectWithIdNotFoundException {
		assertTrue(!writeDao.exists(ETS_DTO_3.getId()) || writeDao.isDisabled(ETS_DTO_3.getId()));
		writeDao.add(ETS_DTO_3);
		assertTrue(writeDao.exists(ETS_DTO_3.getId()));

		final PreparedDto<ExecutableTestSuiteDto> preparedDto = writeDao.getById(ETS_DTO_3.getId());
		assertEquals(ETS_DTO_3.getId(), preparedDto.getDtoId());
		final ExecutableTestSuiteDto dto = preparedDto.getDto();
		assertNotNull(dto);
		assertEquals(ETS_DTO_3.getId(), dto.getId());
		assertEquals(ETS_DTO_3.toString(), dto.toString());

		final PreparedDtoCollection<ExecutableTestSuiteDto> etsCollection = writeDao.getAll(ALL);
		assertNotNull(etsCollection);
		assertNull(etsCollection.get(ETS_DTO_3.getId()));

		assertTrue(writeDao.exists(ETS_DTO_3.getId()));
		assertTrue(writeDao.isDisabled(ETS_DTO_3.getId()));
	}

	@Test
	public void test_2_2_overwrite_disabled() throws StorageException, ObjectWithIdNotFoundException {
		forceDelete(ETS_DTO_3);
		ETS_DTO_3.setDisabled(false);
		writeDao.add(ETS_DTO_3);
		assertTrue(writeDao.exists(ETS_DTO_3.getId()));

		// throw an exception as the item already exists
		boolean exceptionThrown = false;
		try {
			writeDao.add(ETS_DTO_3);
		} catch (StorageException e) {
			exceptionThrown = true;
		}
		assertTrue(exceptionThrown);

		final PreparedDto<ExecutableTestSuiteDto> preparedDto1 = writeDao.getById(ETS_DTO_3.getId());
		assertEquals(ETS_DTO_3.getId(), preparedDto1.getDtoId());
		assertFalse(preparedDto1.getDto().isDisabled());

		// Disable ETS
		writeDao.delete(ETS_DTO_3.getId());
		assertTrue(writeDao.exists(ETS_DTO_3.getId()));
		assertTrue(writeDao.isDisabled(ETS_DTO_3.getId()));

		final PreparedDto<ExecutableTestSuiteDto> preparedDto2 = writeDao.getById(ETS_DTO_3.getId());
		assertEquals(ETS_DTO_3.getId(), preparedDto2.getDtoId());
		assertTrue(preparedDto2.getDto().isDisabled());

		// Can be overwritten now
		ETS_DTO_3.setDisabled(false);
		writeDao.add(ETS_DTO_3);
		assertTrue(writeDao.exists(ETS_DTO_3.getId()));
		assertFalse(writeDao.isDisabled(ETS_DTO_3.getId()));

		final PreparedDto<ExecutableTestSuiteDto> preparedDto3 = writeDao.getById(ETS_DTO_3.getId());
		assertEquals(ETS_DTO_3.getId(), preparedDto3.getDtoId());
		assertFalse(preparedDto3.getDto().isDisabled());
	}

	@Test(timeout = 15000)
	public void test_4_1_streaming_xml()
			throws StorageException, ObjectWithIdNotFoundException, IOException, URISyntaxException {
		compareStreamingContent(ETS_DTO_1, "cmp/ExecutableTestSuiteInItemCollectionResponse.xml", "DsResult2Xml");
	}

	@Test(timeout = 15000)
	public void test_4_2_streaming_json()
			throws StorageException, ObjectWithIdNotFoundException, IOException, URISyntaxException {
		compareStreamingContent(ETS_DTO_1, "cmp/ExecutableTestSuiteInItemCollectionResponse.json", "DsResult2Json");
	}

	@Test(timeout = 15000)
	public void test_7_0_stream_file_to_store()
			throws StorageException, ObjectWithIdNotFoundException, FileNotFoundException, IncompleteDtoException {
		final EID id = EidFactory.getDefault().createAndPreserveStr("61070ae8-13cb-4303-a340-72c8b877b00a");
		forceDelete(writeDao, id);
		final IFile etsFile = new IFile(getClass().getClassLoader().getResource(
				"database/ets.xml").getPath());

		final ExecutableTestSuiteDto etsId = ((StreamWriteDao<ExecutableTestSuiteDto>) writeDao)
				.add(new FileInputStream(etsFile));
		etsId.ensureBasicValidity();

		assertEquals(id.getId(), etsId.getId().getId());

		// Disable afterwards
		writeDao.delete(id);
		assertTrue(writeDao.exists(id));
		assertTrue(writeDao.isDisabled(id));
	}

	@Test(timeout = 15000)
	public void test_8_0_caching_references()
			throws StorageException, ObjectWithIdNotFoundException, FileNotFoundException, IncompleteDtoException {
		BsxTestUtils.forceDeleteAndAdd(ETS_DTO_1, true);
		BsxTestUtils.forceDeleteAndAdd(ETS_DTO_2, true);

		getAndCheckCollection();
		getAndCheckCollection();

		// force cache invalidation
		BsxTestUtils.forceDeleteAndAdd(TAG_DTO_1, false);
		BsxTestUtils.forceDeleteAndAdd(TAG_DTO_2, false);

		getAndCheckCollection();
		getAndCheckCollection();
	}

	private void getAndCheckCollection() throws StorageException {
		final PreparedDtoCollection<ExecutableTestSuiteDto> etsCollection = writeDao.getAll(ALL);
		assertNotNull(etsCollection);

		assertEquals(2, etsCollection.size());

		assertNotNull(etsCollection.get(ETS_DTO_1.getId()));
		assertNotNull(etsCollection.get(ETS_DTO_1.getId()).getTags());
		assertEquals(2, etsCollection.get(ETS_DTO_1.getId()).getTags().size());
		assertEquals(TAG_DTO_1.getId(), etsCollection.get(ETS_DTO_1.getId()).getTags().get(0).getId());
		assertEquals(TAG_DTO_2.getId(), etsCollection.get(ETS_DTO_1.getId()).getTags().get(1).getId());

		assertNotNull(etsCollection.get(ETS_DTO_2.getId()));
		assertNotNull(etsCollection.get(ETS_DTO_2.getId()).getTags());
		assertEquals(2, etsCollection.get(ETS_DTO_2.getId()).getTags().size());
		assertEquals(TAG_DTO_2.getId(), etsCollection.get(ETS_DTO_2.getId()).getTags().get(0).getId());
		assertEquals(TAG_DTO_3.getId(), etsCollection.get(ETS_DTO_2.getId()).getTags().get(1).getId());
	}

}
