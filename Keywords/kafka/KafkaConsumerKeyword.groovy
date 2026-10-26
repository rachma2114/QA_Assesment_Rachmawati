package kafka

import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.util.KeywordUtil

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

import java.time.Duration
import java.util.Collections
import java.util.Properties

class KafkaConsumerKeyword {

	@Keyword
	def consumeMessage() {

		KeywordUtil.logInfo("========== START KAFKA CONSUMER ==========")

		Properties props = new Properties()

		String groupId = "assessment-group-" + System.currentTimeMillis()

		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
		props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName())
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName())
		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")

		KeywordUtil.logInfo("Group ID : " + groupId)

		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props)

		consumer.subscribe(Collections.singletonList("assessment-topic"))

		KeywordUtil.logInfo("Subscribed to assessment-topic")

		// Poll pertama untuk join consumer group
		consumer.poll(Duration.ofMillis(500))

		// Ambil semua partition yang sudah di-assign
		def partitions = consumer.assignment()

		KeywordUtil.logInfo("Assigned Partition : " + partitions.size())

		// Paksa membaca dari awal
		consumer.seekToBeginning(partitions)

		KeywordUtil.logInfo("Reading message from beginning...")

		def records = consumer.poll(Duration.ofSeconds(10))

		KeywordUtil.logInfo("Total message : " + records.count())

		if (records.isEmpty()) {

			consumer.close()
			KeywordUtil.markFailed("No Kafka message received!")

		} else {

			records.each { ConsumerRecord<String, String> record ->

				KeywordUtil.logInfo("--------------------------------------")
				KeywordUtil.logInfo("Topic      : ${record.topic()}")
				KeywordUtil.logInfo("Partition  : ${record.partition()}")
				KeywordUtil.logInfo("Offset     : ${record.offset()}")
				KeywordUtil.logInfo("Key        : ${record.key()}")
				KeywordUtil.logInfo("Value      : ${record.value()}")

			}

			consumer.close()

			KeywordUtil.markPassed(
				"Kafka Consumer successfully received ${records.count()} message(s)."
			)
		}
	}
}