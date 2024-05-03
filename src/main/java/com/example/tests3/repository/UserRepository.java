package com.example.tests3.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.example.tests3.entity.User;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    public User save(User user) {
        dynamoDBMapper.save(user);
        return user;
    }

    public User getUserById(String userId) {
        return dynamoDBMapper.load(User.class, userId);
    }

    public String delete(String userId) {
        User emp = dynamoDBMapper.load(User.class, userId);
        dynamoDBMapper.delete(emp);
        return "User Deleted! ";
    }

    public String update(String userId, User user) {
        dynamoDBMapper.save(user,
                new DynamoDBSaveExpression()
                        .withExpectedEntry("userId",
                                new ExpectedAttributeValue(
                                        new AttributeValue().withS(userId))));
        return userId;
    }

    public User findUserByEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }

        HashMap<String, AttributeValue> eav = new HashMap<>();
        eav.put(":val1", new AttributeValue().withS(email));

        DynamoDBQueryExpression<User> queryExpression = new DynamoDBQueryExpression<User>()
                .withIndexName("EmailIndex")
                .withKeyConditionExpression("email = :val1")
                .withExpressionAttributeValues(eav)
                .withConsistentRead(false);

        List<User> result = dynamoDBMapper.query(User.class, queryExpression);

        return result.isEmpty() ? null : result.get(0);
    }
}
