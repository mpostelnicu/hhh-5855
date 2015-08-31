/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hhh5855;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import hhh5855.model.Child;
import hhh5855.model.Parent;

/**
 * @author vladmihalcea, mpostelnicu
 * 
 * @see https://github.com/vladmihalcea/vladmihalcea.wordpress.com/blob/master/
 *      hibernate-facts/src/test/java/com/vladmihalcea/HibernateBagDuplicateTest
 *      .java
 * 
 *      Hibernate 4.3.11.Final bug test case HHH-5855: "The method "merge
 *      " from the EntityManager causes a duplicated "insert" of a child entity"
 * 
 * @see https://hibernate.atlassian.net/browse/HHH-5855
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Hhh5855Application.class)
public class HHH5855ListChildrenDuplicatesTest {

	private static final Logger LOG = LoggerFactory.getLogger(HHH5855ListChildrenDuplicatesTest.class);

	@PersistenceContext
	private EntityManager entityManager;

	@Autowired
	private TransactionTemplate transactionTemplate;

	/**
	 * This test fails. We add two unsaved children to a loaded parent, then we
	 * merge the parent. Hibernate saves 4 rows instead of two.
	 */
	@Test
	public void test() {

		final Long parentId = cleanAndSaveParent();

		Parent parent = transactionTemplate.execute(new TransactionCallback<Parent>() {
			@Override
			public Parent doInTransaction(TransactionStatus transactionStatus) {
				Parent parent = loadParent(parentId);					
				Child child1 = new Child();
				child1.setName("child1");
				child1.setParent(parent);
				Child child2 = new Child();
				child2.setName("child2");
				child2.setParent(parent);
				parent.addChild(child1);
				parent.addChild(child2);
				entityManager.merge(parent);
				entityManager.flush();
				if (parent.getChildren().size() == 4) {
					LOG.error("!!! DUPLICATE ROWS GENERATED !!!");
				}
				return parent;
			}
		});

		assertEquals(2, parent.getChildren().size());
	}

	/**
	 * This test will work fine. We persist the children first, then call merge on
	 * the parent
	 */
	@Test
	public void testFixByPersistingChild() {
		final Long parentId = cleanAndSaveParent();

		Parent parent = transactionTemplate.execute(new TransactionCallback<Parent>() {
			@Override
			public Parent doInTransaction(TransactionStatus transactionStatus) {
				Parent parent = loadParent(parentId);
				Child child1 = new Child();
				child1.setName("child1");
				Child child2 = new Child();
				child2.setName("child2");
				entityManager.persist(child1);
				entityManager.persist(child2);
				parent.addChild(child1);
				parent.addChild(child2);
				entityManager.merge(parent);
				parent.getChildren().size();
				return parent;
			}
		});
		assertEquals(2, parent.getChildren().size());
	}

	protected Long cleanAndSaveParent() {
		return transactionTemplate.execute(new TransactionCallback<Long>() {
			@Override
			public Long doInTransaction(TransactionStatus transactionStatus) {
				entityManager.createQuery("delete from Child where id > 0").executeUpdate();
				entityManager.createQuery("delete from Parent where id > 0").executeUpdate();
				assertTrue(entityManager.createQuery("from Parent").getResultList().isEmpty());
				Parent parent = new Parent();
				entityManager.persist(parent);
				entityManager.flush();
				return parent.getId();
			}
		});
	}

	protected Parent loadParent(Long parentId) {
		return entityManager.find(Parent.class, parentId);
	}

}
