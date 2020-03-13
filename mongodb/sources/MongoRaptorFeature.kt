package io.fluidsonic.raptor

import com.mongodb.*
import com.mongodb.client.gridfs.codecs.*
import com.mongodb.client.model.geojson.codecs.*
import org.bson.codecs.*


object MongoRaptorFeature : RaptorFeature {

	override fun RaptorFeatureSetup.setup() {
		bson {
			providers(
				DBRefCodecProvider(),
				DBObjectCodecProvider(),
				DocumentCodecProvider(DocumentToDBRefTransformer()),
				IterableCodecProvider(DocumentToDBRefTransformer()),
				MapCodecProvider(DocumentToDBRefTransformer()),
				GeoJsonCodecProvider(),
				GridFSFileCodecProvider()
			)
		}
	}
}
